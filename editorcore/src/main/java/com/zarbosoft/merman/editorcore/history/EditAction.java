package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;

public abstract class EditAction extends Action {
    public final History history;

    public EditAction(History history) {
        this.history = history;
    }

    @Override
    public final boolean run(Context context) {
        history.finishChange(context);
        boolean out = run1(context);
        history.finishChange(context);
        return out;
    }

    protected abstract boolean run1(Context context);
}
