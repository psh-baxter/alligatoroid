package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public abstract class Cursor {
  protected abstract void clear(Context context);

  public abstract Visual getVisual();

  public abstract SelectionState saveState();

  public abstract SyntaxPath getSyntaxPath();

  public abstract void dispatch(Dispatcher dispatcher);

  public interface Dispatcher {

    void handle(VisualFrontArray.ArrayCursor cursor);

    void handle(VisualFrontAtomBase.NestedCursor cursor);

    void handle(VisualFrontPrimitive.PrimitiveCursor cursor);
  }
}
