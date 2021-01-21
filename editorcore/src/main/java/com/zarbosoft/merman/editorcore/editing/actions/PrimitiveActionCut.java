package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

@Action.StaticID(id = "cut")
public class PrimitiveActionCut extends EditAction {
  private final VisualFrontPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionCut(EditingExtension edit, VisualFrontPrimitive.PrimitiveCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
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
