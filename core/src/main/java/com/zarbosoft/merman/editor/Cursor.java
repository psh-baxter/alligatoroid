package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.ROSet;

public abstract class Cursor {
  protected abstract void clear(Context context);

  public abstract Visual getVisual();

    public abstract SelectionState saveState();

  public abstract Path getSyntaxPath();

  public interface Dispatcher {

    void handle(VisualFrontArray.ArrayCursor cursor);

    void handle(VisualFrontAtomBase.NestedCursor cursor);

    void handle(VisualFrontPrimitive.PrimitiveCursor cursor);
  }

  public abstract void dispatch(Dispatcher dispatcher);
}
