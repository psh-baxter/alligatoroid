package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

public class ArrayActionDelete extends EditAction {
    public String id() {
        return "delete";
    }
  private final VisualFrontArray.ArrayCursor cursor;

  public ArrayActionDelete(EditingExtension edit, VisualFrontArray.ArrayCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
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
