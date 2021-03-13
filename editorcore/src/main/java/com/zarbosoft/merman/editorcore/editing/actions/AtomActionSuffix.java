package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

public class AtomActionSuffix extends EditAction {
    public String id() {
        return "suffix";
    }
  private final VisualFrontAtomBase base;

  public AtomActionSuffix(EditingExtension edit, VisualFrontAtomBase base) {
    super(edit);
    this.base = base;
  }

  @Override
  public void run1(final Context context) {
    final Atom old = base.atomGet();
    final Atom gap = edit.suffixGap.create();
    edit.atomSet(context, edit.history, base, gap);
    edit.history.apply(
        context,
        new ChangeArray((FieldArray) gap.fields.getOpt("value"), 0, 0, ImmutableList.of(old)));
    gap.fields.getOpt("gap").selectInto(context);
    return true;
  }
}
