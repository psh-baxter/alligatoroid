package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

public class PrimitiveActionDeleteNext extends EditAction {
    public String id() {
        return "delete_next";
    }
  private final VisualFrontPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionDeleteNext(EditingExtension edit, VisualFrontPrimitive.PrimitiveCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    if (cursor.range.beginOffset == cursor.range.endOffset) {
      if (cursor.range.endOffset == cursor.visualPrimitive.value.length()) return false;
      final int following = cursor.following();
      edit.history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              following - cursor.range.beginOffset));
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
