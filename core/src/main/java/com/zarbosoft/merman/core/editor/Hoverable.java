package com.zarbosoft.merman.core.editor;

import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualAtom;

public abstract class Hoverable {
  protected abstract void clear(Context context);

  /**
   * Always returns a path to an atom
   * @return
   */
  public abstract Path getSyntaxPath();

  public abstract void select(Context context);

  public abstract VisualAtom atom();

  public abstract Visual visual();
}
