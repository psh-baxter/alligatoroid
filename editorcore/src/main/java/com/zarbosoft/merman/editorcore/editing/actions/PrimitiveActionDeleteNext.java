package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

@Action.StaticID(id = "delete_next")
public class PrimitiveActionDeleteNext extends EditAction {
  private final VisualPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionDeleteNext(History history, VisualPrimitive.PrimitiveCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    if (cursor.range.beginOffset == cursor.range.endOffset) {
      if (cursor.range.endOffset == cursor.visualPrimitive.value.length()) return false;
      final int following = cursor.following();
      history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              following - cursor.range.beginOffset));
    } else
      history.apply(
          context,
          new ChangePrimitiveRemove(
              cursor.visualPrimitive.value,
              cursor.range.beginOffset,
              cursor.range.endOffset - cursor.range.beginOffset));
    return true;
  }
}
