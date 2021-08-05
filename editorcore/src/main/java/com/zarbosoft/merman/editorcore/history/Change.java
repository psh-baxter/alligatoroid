package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.editorcore.Editor;

public abstract class Change {
  public abstract boolean merge(Change other);

  public abstract Change apply(Editor editor);
}
