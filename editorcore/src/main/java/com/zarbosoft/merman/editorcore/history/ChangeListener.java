package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;

public abstract class ChangeListener {
    public abstract void applied(Context context, Change change);
}
