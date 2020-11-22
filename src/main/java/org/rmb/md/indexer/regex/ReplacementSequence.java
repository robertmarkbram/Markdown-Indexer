package org.rmb.md.indexer.regex;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.rmb.md.indexer.regex.Replacement.PREFIX_FIND;
import static org.rmb.md.indexer.regex.Replacement.PREFIX_REPLACE;
import static org.rmb.md.indexer.regex.Replacement.buildReplacement;

/**
 * Sequence and regular expression based find and replace actions to apply to a string.
 */
@Slf4j
public final class ReplacementSequence {

   /**
    * The Replacement list.
    */
   private final List<Replacement> replacementList;

   /**
    * Create replacement sequence from string path to replacement sequence file.
    *
    * @param path string path to file with find/replace strings.
    *
    * @throws IOException        IO exception if we cannot read the file
    * @throws URISyntaxException the uri syntax exception if we cannot create URL to file from <code>path</code>
    */
   public ReplacementSequence(final String path) throws IOException, URISyntaxException {
      this(Paths.get(ReplacementSequence.class.getResource(path).toURI()));
   }

   /**
    * Create replacement sequence from {@link Path} to replacement sequence file.
    *
    * @param path path to file with find/replace strings
    *
    * @throws IOException           IO exception if we cannot read the file
    * @throws IllegalStateException if the file contains an uneven list of them, or a {@link Replacement#PREFIX_REPLACE}
    *                               before a {@link Replacement#PREFIX_FIND}.
    */
   public ReplacementSequence(final Path path) throws IOException {
      this(readReplacements(path));
   }

   /**
    * Create replacement sequence from list of {@link Replacement}s.
    *
    * @param replacementList the replacement list
    */
   public ReplacementSequence(final List<Replacement> replacementList) {
      this.replacementList = Collections.unmodifiableList(replacementList);
   }

   /**
    * Read replacements from a file.
    *
    * @param path to the file containing replacement texts.
    *
    * @return list of replacements.
    *
    * @throws IOException the io exception if there is a problem reading from a file.
    */
   private static List<Replacement> readReplacements(final Path path) throws IOException {
      final var lines = Files.readAllLines(path).stream()
            .filter(ReplacementSequence::isFindReplaceLine).collect(Collectors.toList());
      if (lines.size() % 2 != 0) {
         throw new IllegalArgumentException("List of find/replace strings was uneven ("
               + lines.size() + " tokens).");
      }
      final var replacementListTmp = new ArrayList<Replacement>();
      for (int index = 0; index < lines.size(); index += 2) {
         final var replacement = buildReplacement(lines.get(index), lines.get(index + 1));
         log.trace("Found find [{}] and replace [{}].", replacement.find(), replacement.replace());
         replacementListTmp.add(replacement);
      }
      return replacementListTmp;
   }

   /**
    * Is find replace line boolean.
    *
    * @param line the line to be tested
    *
    * @return true if <code>line</code> starts with {@link Replacement#PREFIX_FIND} or
    * {@link Replacement#PREFIX_REPLACE}
    */
   private static boolean isFindReplaceLine(final String line) {
      return isNotBlank(line) && (line.startsWith(PREFIX_FIND) || line.startsWith(PREFIX_REPLACE));
   }

   /**
    * Apply replacements to <code>string</code>.
    *
    * @param string the string that replacements will be applied to
    *
    * @return result of applying replacements to <code>string</code>
    */
   public String apply(final String string) {
      log.trace("Applying replacements to {}", string);
      var result = string;
      for (Replacement replacement : replacementList) {
         result = result.replaceAll(replacement.find(), replacement.replace());
         log.trace("After replacement {} we have result: [{}]", replacement, result);
      }
      return result;
   }

   @Override
   public String toString() {
      return "ReplacementSequence{replacementList=" + replacementList + '}';
   }

}
