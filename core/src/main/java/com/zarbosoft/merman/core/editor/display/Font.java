package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.editor.Context;

public interface Font {
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
