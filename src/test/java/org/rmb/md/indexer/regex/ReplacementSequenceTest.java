package org.rmb.md.indexer.regex;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Replacement sequences.
 */
class ReplacementSequenceTest {

   /**
    * Test empty sequence - no replacements.
    */
   @Test
   void testEmptySequence() {
      var replacementSequence = new ReplacementSequence(Collections.emptyList());
      final var expected = "test";
      assertEquals(expected, replacementSequence.apply(expected));
   }

   /**
    * Test basic string replace, not a regex.
    */
   @Test
   void testBasicSequence() {
      final var find = "test";
      final var replace = "rob";
      var replacementSequence = new ReplacementSequence(List.of(
            new Replacement(find, replace)
      ));
      assertEquals(replace, replacementSequence.apply(find));
   }

   /**
    * Test simple regex replace.
    */
   @Test
   void testSimpleReqex() {
      final var find = "^.*$";
      final var replace = "rob";
      var replacementSequence = new ReplacementSequence(List.of(
            new Replacement(find, replace)
      ));
      final var start = "test";
      assertEquals(replace, replacementSequence.apply(start));
   }

   /**
    * Test regex replace with back-references.
    */
   @Test
   void testBackReferenceReqex() {
      final var find = "(\\w++) (\\w++) (\\w++)";
      final var replace = "$3 $2 $1";
      var replacementSequence = new ReplacementSequence(List.of(
            new Replacement(find, replace)
      ));
      final var start = "Robert Mark Bram";
      assertEquals("Bram Mark Robert", replacementSequence.apply(start));
   }

   /**
    * Test multiple regular expressions in a sequence.
    */
   @Test
   void testReqexSequence() {
      var replacementSequence = new ReplacementSequence(List.of(
            new Replacement("(\\w++) (\\w++) (\\w++)", "$3 $2 $1"),
            new Replacement("^[\\s]*", ""),
            new Replacement("[\\s]*$", "")
      ));
      final var start = "\t  \t  Robert Mark Bram   \t   ";
      assertEquals("Bram Mark Robert", replacementSequence.apply(start));
   }

}
