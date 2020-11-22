package org.rmb.md.indexer.regex;

/**
 * A single replacement with a <code>find</code> and <code>replace</code> string.
 */
public record Replacement(String find, String replace) {

   /**
    * A line that that starts with this key will be the {@link Replacement#find()} part of a {@link Replacement}.
    */
   public static final String PREFIX_FIND = "find=";

   /**
    * A line that that starts with this key will be the {@link Replacement#replace()} ()} part of a {@link
    * Replacement}.
    */
   public static final String PREFIX_REPLACE = "replace=";

   /**
    * A line in a replacement sequence file can end with this to preserve trailing spaces: <code>;;;</code>.
    */
   public static final String SUFFIX_LINE_TERMINATOR = ";;;";

   /**
    * Build a {@link Replacement} from strings read from a replacement file. Will remove prefixes:
    *
    * @param findWithExtra    the find with extra
    * @param replaceWithExtra the replace with extra
    *
    * @return the replacement
    */
   public static Replacement buildReplacement(final String findWithExtra, final String replaceWithExtra) {
      return new Replacement(
            findWithExtra.replaceAll("^" + PREFIX_FIND, "").replaceAll(SUFFIX_LINE_TERMINATOR + "$", ""),
            replaceWithExtra.replaceAll("^" + PREFIX_REPLACE, "").replaceAll(SUFFIX_LINE_TERMINATOR + "$", ""));
   }

}
