package com.zarbosoft.merman.extensions;

public abstract class Extension {

  public abstract State create(ExtensionContext context);

  /**
   * Since multiple documents may use the same syntax, modules may be initialized and independently
   * destroyed multiple times from the same definition. All state should go in the State object.
   */
  public abstract static class State {
    public abstract void destroy(ExtensionContext context);
  }
}
