package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public interface Font {
  double getAscent();

  double getDescent();

  public Measurer measurer();

  public interface Measurer {
    public double getWidth(String text);

    /**
     * Get the nearest index to the converse - so halfway through 1 = 0, halfway through last = last
     * @param context
     * @param converse
     * @return
     */
    public int getIndexAtConverse(Context context, String text, double converse);
  }
}
