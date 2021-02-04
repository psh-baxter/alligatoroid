package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;

public abstract class EditAction implements Action {
  protected final EditingExtension edit;

  public EditAction(EditingExtension edit) {
    this.edit = edit;
  }

  @Override
  public final boolean run(Context context) {
    edit.history.finishChange(context);
    boolean out = run1(context);
    edit.history.finishChange(context);
    return out;
  }

  protected abstract boolean run1(Context context);
}
