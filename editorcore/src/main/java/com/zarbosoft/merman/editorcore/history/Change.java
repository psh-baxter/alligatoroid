package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;

public abstract class Change {
  public abstract boolean merge(Change other);

  public abstract Change apply(Context context);
}
