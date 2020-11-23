package com.zarbosoft.merman.editor.history;

import com.zarbosoft.merman.editor.Context;

public abstract class Change {
  public abstract boolean merge(Change other);

  public abstract Change apply(Context context);
}
