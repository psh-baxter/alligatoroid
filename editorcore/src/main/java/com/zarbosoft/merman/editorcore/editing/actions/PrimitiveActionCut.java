package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

public class PrimitiveActionCut extends EditAction {
    public String id() {
        return "cut";
    }
  private final VisualFrontPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionCut(EditingExtension edit, VisualFrontPrimitive.PrimitiveCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    context.copy(
        cursor
            .visualPrimitive
            .value
            .get()
            .substring(cursor.range.beginOffset, cursor.range.endOffset));
    edit.history.apply(
        context,
        new ChangePrimitiveRemove(
            cursor.visualPrimitive.value,
            cursor.range.beginOffset,
            cursor.range.endOffset - cursor.range.beginOffset));
    return true;
  }
}
