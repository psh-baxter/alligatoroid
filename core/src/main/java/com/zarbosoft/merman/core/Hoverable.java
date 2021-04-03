package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;

public abstract class Hoverable {
  protected abstract void clear(Context context);

  /**
   * Always returns a path to an atom
   * @return
   */
  public abstract SyntaxPath getSyntaxPath();

  public abstract void select(Context context);

  public abstract VisualAtom atom();

  public abstract Visual visual();
}
