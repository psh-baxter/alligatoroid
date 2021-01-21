package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;

@Action.StaticID(id = "delete")
public class AtomActionDelete extends EditAction {
  private final VisualFrontAtomBase base;

  public AtomActionDelete(EditingExtension edit, VisualFrontAtomBase base) {
    super(edit);
    this.base = base;
  }

  @Override
  public boolean run1(final Context context) {
    edit.atomSet(context, edit.history, base, edit.gap.create());
    return true;
  }
}
