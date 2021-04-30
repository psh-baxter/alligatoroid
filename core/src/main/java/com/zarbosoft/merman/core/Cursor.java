package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public abstract class Cursor {
  protected abstract void destroy(Context context);

  public abstract Visual getVisual();

  public abstract SelectionState saveState();

  public abstract SyntaxPath getSyntaxPath();

  public abstract void dispatch(Dispatcher dispatcher);

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    return false;
  }

  public void handleTyping(Context context, String text) {}

  public interface Dispatcher {

    void handle(VisualFrontArray.Cursor cursor);

    void handle(VisualFrontAtomBase.Cursor cursor);

    void handle(VisualFrontPrimitive.Cursor cursor);
  }
}
