package com.zarbosoft.merman.modules;

import com.zarbosoft.merman.editor.Context;

public abstract class Module {

  public abstract State initialize(Context context);

  /**
   * Since multiple documents may use the same syntax, modules may be initialized and independently
   * destroyed multiple times from the same definition. All state should go in the State object.
   */
  public abstract static class State {
    public abstract void destroy(Context context);
  }
}
