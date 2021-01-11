package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

@Action.StaticID(id = "cut")
public class ArrayActionCut extends EditAction {
  private final VisualArray.ArrayCursor cursor;

  public ArrayActionCut(History history, VisualArray.ArrayCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    context.copy(cursor.self.value.data.subList(cursor.beginIndex, cursor.endIndex + 1));
    history.apply(
        context,
        new ChangeArray(
            cursor.self.value,
            cursor.beginIndex,
            cursor.endIndex - cursor.beginIndex + 1,
            ImmutableList.of()));
    return true;
  }
}
