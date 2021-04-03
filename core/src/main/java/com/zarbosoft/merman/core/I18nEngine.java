package com.zarbosoft.merman.core;

public interface I18nEngine {
  public static final int DONE = -1;

  public static interface Walker {
    /**
     * Next split before offset, -1 if none
     *
     * Includes 0 and string.size()
     *
     * @param offset
     * @return
     */
    int preceding(int offset);

    /**
     * Next split after offset, -1 if none
     *
     * Includes 0 and string.size()
     *
     * @param offset
     * @return
     */
    int following(int offset);
  }

  /**
   * Split the string into glyphs
   *
   * @return
   * @param s
   */
  public Walker glyphWalker(String s);

  /**
   * Split the string into word-ish things
   *
   * @return
   * @param s
   */
  public Walker wordWalker(String s);

  /**
   * Split the string into good places to break a line
   *
   * @return
   * @param s
   */
  public Walker lineWalker(String s);
}
