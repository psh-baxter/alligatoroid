package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.Visual;

public abstract class Cursor {
  protected abstract void destroy(Context context);

  /**
   * Get the visual that owns the cursor
   * @return
   */
  public abstract Visual getVisual();

  public abstract CursorState saveState();

  public abstract SyntaxPath getSyntaxPath();

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    return false;
  }

  public void handleTyping(Context context, String text) {}
}
