package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

@Action.StaticID(id = "split")
public class PrimitiveActionSplitLines extends EditAction {
  private final VisualPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionSplitLines(History history, VisualPrimitive.PrimitiveCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    if (cursor.range.beginOffset != cursor.range.endOffset)
      history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              cursor.range.endOffset - cursor.range.beginOffset));
    history.apply(
        context,
        new ChangePrimitiveAdd(cursor.visualPrimitive.value, cursor.range.beginOffset, "\n"));
    return true;
  }
}
