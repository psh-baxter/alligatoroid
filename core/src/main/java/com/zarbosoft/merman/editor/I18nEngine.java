package com.zarbosoft.merman.editor;

public interface I18nEngine {
  public static final int DONE = -1;

  public static interface Walker {
    /**
     * Next split before offset, -1 if none
     *
     * @param offset
     * @return
     */
    int preceding(int offset);

    /**
     * Next split after offset, -1 if none
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
