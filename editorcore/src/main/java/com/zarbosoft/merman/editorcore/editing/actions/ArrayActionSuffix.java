package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.editing.BaseGapAtomType;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.TSList;

public class ArrayActionSuffix extends EditAction {
    public String id() {
        return "suffix";
    }
  private final VisualFrontArray.ArrayCursor cursor;

  public ArrayActionSuffix(EditingExtension edit, VisualFrontArray.ArrayCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    final Atom gap = edit.suffixGap.create();
    TSList<Atom> transplant =
        cursor.visual.value.data.sublist(cursor.beginIndex, cursor.endIndex).mut();
    edit.history.apply(
        context,
        new ChangeArray(
            cursor.visual.value,
            cursor.beginIndex,
            cursor.endIndex - cursor.beginIndex,
            ImmutableList.of(gap)));
    edit.history.apply(
        context,
        new ChangeArray(
            (FieldArray) gap.fields.get(BaseGapAtomType.GAP_PRIMITIVE_KEY), 0, 0, transplant));
    gap.fields.getOpt("gap").selectInto(context);
    return true;
  }
}
