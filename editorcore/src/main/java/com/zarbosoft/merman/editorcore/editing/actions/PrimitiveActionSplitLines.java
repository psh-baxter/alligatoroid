package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

public class PrimitiveActionSplitLines extends EditAction {
    public String id() {
        return "split";
    }
  private final VisualFrontPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionSplitLines(EditingExtension edit, VisualFrontPrimitive.PrimitiveCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    if (cursor.range.beginOffset != cursor.range.endOffset)
      edit.history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              cursor.range.endOffset - cursor.range.beginOffset));
    edit.history.apply(
        context,
        new ChangePrimitiveAdd(cursor.visualPrimitive.value, cursor.range.beginOffset, "\n"));
    return true;
  }
}
