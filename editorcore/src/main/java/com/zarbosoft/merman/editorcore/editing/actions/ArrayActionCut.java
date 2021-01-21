package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

@Action.StaticID(id = "cut")
public class ArrayActionCut extends EditAction {
  private final VisualFrontArray.ArrayCursor cursor;

  public ArrayActionCut(EditingExtension edit, VisualFrontArray.ArrayCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    context.copy(cursor.self.value.data.subList(cursor.beginIndex, cursor.endIndex + 1));
    edit.history.apply(
        context,
        new ChangeArray(
            cursor.self.value,
            cursor.beginIndex,
            cursor.endIndex - cursor.beginIndex + 1,
            ImmutableList.of()));
    return true;
  }
}
