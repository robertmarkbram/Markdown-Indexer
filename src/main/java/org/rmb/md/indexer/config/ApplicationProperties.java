package org.rmb.md.indexer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperties {

   /**
    * Path to folder containing markdown files that we will index.
    */
   private String pathToMarkdownFiles;

   /**
    * File containing replacement sequence to apply to each markdown path found.
    */
   private String pathToMarkdownFileReplacementSequence;

   /**
    * File containing replacement sequence to apply to each markdown URL output.
    */
   private String pathToMarkdownUrlReplacementSequence;

   /**
    * File containing replacement sequence to apply to each markdown heading found.
    */
   private String pathToMarkdownHeadingReplacementSequence;

   /**
    * The Path to where we will write URL files to.
    */
   private String pathToUrls;

   /**
    * Base URL for some webapp (like Hug) that serves the markdown files as content.
    */
   private String webappBaseUrl;

}
