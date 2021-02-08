package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

public class PrimitiveActionDeletePrevious extends EditAction {
    public String id() {
        return "delete_previous";
    }
  private final VisualFrontPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionDeletePrevious(
      EditingExtension edit, VisualFrontPrimitive.PrimitiveCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    if (cursor.range.beginOffset == cursor.range.endOffset) {
      if (cursor.range.beginOffset == 0) return false;
      final int preceding = cursor.preceding();
      edit.history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value, preceding, cursor.range.beginOffset - preceding));
    } else
      edit.history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              cursor.range.endOffset - cursor.range.beginOffset));
    return true;
  }
}
