package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;

public class AtomActionCut extends EditAction {
    public String id() {
        return "cut";
    }
  private final VisualFrontAtomBase base;

  public AtomActionCut(EditingExtension edit, VisualFrontAtomBase base) {
    super(edit);
    this.base = base;
  }

  @Override
  public void run1(final Context context) {
    context.copy(ImmutableList.of(base.atomGet()));
    edit.atomSet(context, edit.history, base, edit.gap.create());
    return true;
  }
}
