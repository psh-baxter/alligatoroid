package com.zarbosoft.merman.editor;

public interface I18nEngine {
  public static final int DONE = -1;

  public static interface Walker {
    void setText(String text);

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
   */
  public Walker glyphWalker();

  /**
   * Split the string into word-ish things
   *
   * @return
   */
  public Walker wordWalker();

  /**
   * Split the string into good places to break a line
   *
   * @return
   */
  public Walker lineWalker();
}
