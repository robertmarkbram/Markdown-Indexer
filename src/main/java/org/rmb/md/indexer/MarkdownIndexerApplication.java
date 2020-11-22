package org.rmb.md.indexer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rmb.md.indexer.regex.ReplacementSequence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Index a directory of markdown files (for an app such as <a href="https://gohugo.io/">Hugo</a>) as a set of URL files
 * that
 * can be indexed with a program such as <a href="https://keypirinha.com/">KeyPirinha</a>.
 */
@SpringBootApplication
@Slf4j
public class MarkdownIndexerApplication implements CommandLineRunner {

   /**
    * Finds markdown files - Cheat markdown.
    */
   public static final PathMatcher MD_MATCHER = FileSystems.getDefault().getPathMatcher(
         "regex:^.*(\\\\cheat_|\\\\project-tech-tips\\\\).*.md$");

   /**
    * Finds Hugo URL files.
    */
   public static final PathMatcher HUGO_URL_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**Hugo - *.url");

   /**
    * Path to folder containing markdown files that we will index.
    */
   @Value("${application.path-to-markdown-files}")
   private String pathToMarkdownFiles;

   /**
    * File containing replacement sequence to apply to each markdown path found.
    */
   @Value("${application.path-to-markdown-file-replacement-sequence}")
   private String pathToMarkdownFileReplacementSequence;

   /**
    * File containing replacement sequence to apply to each markdown URL output.
    */
   @Value("${application.markdown-url-replacement-sequence}")
   private String pathToMarkdownUrlReplacementSequence;

   /**
    * File containing replacement sequence to apply to each markdown heading found.
    */
   @Value("${application.path-to-markdown-heading-replacement-sequence}")
   private String pathToMarkdownHeadingReplacementSequence;

   /**
    * The Path to where we will write URL files to.
    */
   @Value("${application.path-to-urls}")
   private String pathToUrls;

   /**
    * Base URL for some webapp (like Hug) that serves the markdown files as content.
    */
   @Value("${application.webapp-base-url}")
   private String webappBaseUrl;

   /**
    * The replacement sequence to apply against markdown paths.
    */
   private ReplacementSequence replacementSequenceMd;

   /**
    * The replacement sequence to apply against markdown URLs.
    */
   private ReplacementSequence replacementSequenceUrl;

   /**
    * The replacement sequence to apply against markdown headings.
    */
   private ReplacementSequence replacementSequenceHeadings;

   public static void main(String[] args) {
      SpringApplication.run(MarkdownIndexerApplication.class, args).close();
   }

   @Override
   public void run(final String... args) {
      log.debug("""
                Application starting.
                   path to markdown file: {}
                   web-app base URL: {}
                   path to URL folder: {}
                """, pathToMarkdownFiles, webappBaseUrl, pathToUrls);

      init();
      deleteShortcuts();

      try (Stream<Path> files = Files.walk(Paths.get(pathToMarkdownFiles))) {

         // traverse all files and sub-folders
         files.map(Path::toAbsolutePath)
               .filter(this::wantThisFile)
               .forEach(this::processFile);

      } catch (IOException ex) {
         log.error("Error while traversing Hugo files", ex);
      }

      log.debug("Application finished.");
   }

   /**
    * Initialise required state.
    */
   private void init() {
      replacementSequenceMd =
            readMdReplacementSequence("markdown files", pathToMarkdownFileReplacementSequence).orElseThrow();
      replacementSequenceUrl =
            readMdReplacementSequence("markdown URLs", pathToMarkdownUrlReplacementSequence).orElseThrow();
      replacementSequenceHeadings =
            readMdReplacementSequence("markdown headings", pathToMarkdownHeadingReplacementSequence).orElseThrow();
   }

   /**
    * Delete current Hugo shortcuts.
    */
   private void deleteShortcuts() {
      try (Stream<Path> files = Files.walk(Paths.get(pathToUrls))) {
         files.filter(HUGO_URL_MATCHER::matches)
               .forEach(path -> {
                  log.debug("Delete file: {}", path);
                  try {
                     Files.deleteIfExists(path);
                  } catch (IOException e) {
                     log.error("Failed to delete file: {}", path, e);
                  }
               });
      } catch (IOException ex) {
         log.error("Error while traversing Hugo files to delete them", ex);
      }
   }

   /**
    * Do I want this file?
    *
    * @param path that I may wish to ignore.
    *
    * @return true if I want to ignore this path
    */
   private boolean wantThisFile(final Path path) {
      log.trace("Checking if I want this path: {}", path.toAbsolutePath().toString());
      // Only want markdown files.
      if (!MD_MATCHER.matches(path)) {
         return false;
      }

      // Hard-coded file names I don't want.
      if (switch (path.getFileName().toString()) {
         case "_index.md", "_footer.md" -> true;
         default -> false;
      }) {
         return false;
      }

      // Check other sorts of files I don't want.
      final var fileName = path.toAbsolutePath().toString();
      return !fileName.endsWith("java.md")
            && !fileName.endsWith("yml.md")
            && !fileName.contains(File.separator + "current-issue" + File.separator);
      // OK, use it.
   }

   /**
    * Process file and write out URL.
    *
    * @param path the path
    */
   private void processFile(final Path path) {
      log.debug("Processing path: {}", path);
      createShortcutForMarkdownFile(path);
   }

   /**
    * Create replacement sequence.
    *
    * @param label the label for debugging purposes only
    * @param path  string path to file
    *
    * @return optional wrapped around {@link ReplacementSequence}, which will be empty if there was an error creating
    * it.
    */
   private Optional<ReplacementSequence> readMdReplacementSequence(final String label, final String path) {
      try {
         var replacementSequence = new ReplacementSequence(path);
         log.debug("Found replacements for {}: {}", label, replacementSequence);
         return Optional.of(replacementSequence);
      } catch (IOException | URISyntaxException e) {
         log.error("FATAL: failed to create replacement sequence for " + label + ".", e);
         return Optional.empty();
      }
   }

   /**
    * Create shortcut file.
    *
    * @param path path to the file we are creating a shortcut to
    */
   private void createShortcutForMarkdownFile(final Path path) {
      log.trace("Path: {}", path);
      var fileName = "Hugo - " +
            replacementSequenceMd.apply(path.toAbsolutePath().toString().replace(pathToMarkdownFiles + "\\", ""))
            + ".url";
      var url = webappBaseUrl
            + replacementSequenceUrl.apply(path.toAbsolutePath().toString().replace(pathToMarkdownFiles + "\\", ""));
      createShortcutForMarkdownFile(fileName, url);
      createShortcutForMarkdownFileHeaders(path, fileName, url);
   }

   /**
    * Create shortcut file - with the <code>url</code> extension.
    *
    * @param fileName name of file which will be created in the <code>application.path-to-urls</code> directory
    * @param url      URL that the shortcut will point to
    */
   private void createShortcutForMarkdownFile(final String fileName, final String url) {
      log.debug("URL file name: {}", fileName);
      log.debug("URL: {}", url);

      // Write out URL file.
      try {
         Files.write(
               Paths.get(pathToUrls, fileName),
               ("""
                [InternetShortcut]
                URL=""" + url).getBytes());
      } catch (IOException e) {
         log.error("Failed to write URL file for path {}", fileName, e);
      }
   }

   /**
    * Read markdown file and create shortcut to point to each heading in the file.
    *
    * @param path the path
    */
   private void createShortcutForMarkdownFileHeaders(final Path path, final String fileName, final String url) {
      try {
         Files.readAllLines(path).stream().filter(MarkdownIndexerApplication::lineIsMarkdownHeading)
               .map(replacementSequenceHeadings::apply)
               .filter(StringUtils::isNotBlank)
               .forEach(heading -> createShortcutForMarkdownHeader(fileName, url, heading));

      } catch (IOException e) {
         log.error("Failed to read contents of file: " + path.toAbsolutePath().toString(), e);
      }
   }

   /**
    * Tests if a Line is a markdown heading.
    *
    * @param line read from a file
    *
    * @return true if the line represents a markdown heading we want to create a shortcut for
    */
   private static boolean lineIsMarkdownHeading(String line) {
      if (isBlank(line)) {
         return false;
      }
      return line.startsWith("#");
   }

   /**
    * Create shortcut file - with the <code>url</code> extension.
    *
    * @param fileName base name of file which will be created in the <code>application.path-to-urls</code> directory.
    * @param url      base URL that the shortcut will point to
    * @param heading  the heading for which a new file name and url will be created, building on <code>fileName</code>
    *                 and <code >url</code>
    */
   private void createShortcutForMarkdownHeader(final String fileName, final String url, final String heading) {
      log.debug("URL file name: {}", fileName);
      log.debug("URL: {}", url);
      log.debug("Heading: {}", heading);

      /* 
         1. Apply heading replacement sequence. 
         2. Replace all space with a hyphen.
         3. Change all to lower case.
         4. Remove any characters matching [^a-z0-9-]
       */

      final var headingFileName = fileName.replaceAll(".url$", "") + " - "
            + heading.replaceAll("[^a-zA-Z0-9- ]", "")
            + ".url";
      log.debug("Heading file name: {}", headingFileName);

      final var headingUrl = url + "/#" + replacementSequenceHeadings.apply(heading)
            .trim()
            .replaceAll(" ", "-")
            .toLowerCase()
            .replaceAll("[^a-z0-9-]", "");
      log.debug("Heading URL: {}", headingUrl);


      // Write out URL file.
      try {
         Files.write(
               Paths.get(pathToUrls,
                     headingFileName),
               ("""
                [InternetShortcut]
                URL=""" + headingUrl).getBytes());
      } catch (IOException e) {
         log.error("Failed to write URL file for path {}", fileName, e);
      }
   }

}