package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;

@Action.StaticID(id = "delete")
public class AtomActionDelete extends EditAction {
  private final EditingExtension.AtomSet set;

  public AtomActionDelete(History history, EditingExtension.AtomSet set) {
    super(history);
    this.set = set;
  }

  @Override
  public boolean run1(final Context context) {
    set.set(context, context.syntax.gap.create());
    return true;
  }
}
