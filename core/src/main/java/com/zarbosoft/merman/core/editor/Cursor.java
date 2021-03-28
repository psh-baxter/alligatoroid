package com.zarbosoft.merman.core.editor;

import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontPrimitive;

public abstract class Cursor {
  protected abstract void clear(Context context);

  public abstract Visual getVisual();

  public abstract SelectionState saveState();

  public abstract Path getSyntaxPath();

  public abstract void dispatch(Dispatcher dispatcher);

  public interface Dispatcher {

    void handle(VisualFrontArray.ArrayCursor cursor);

    void handle(VisualFrontAtomBase.NestedCursor cursor);

    void handle(VisualFrontPrimitive.PrimitiveCursor cursor);
  }
}
