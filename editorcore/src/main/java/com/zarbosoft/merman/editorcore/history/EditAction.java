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
  public final void run(Context context) {
    edit.history.finishChange(context);
    run1(context);
    edit.history.finishChange(context);
  }

  protected abstract void run1(Context context);
}
