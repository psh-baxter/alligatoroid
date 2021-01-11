package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;

@Action.StaticID(id = "paste")
public class PrimitiveActionPaste extends EditAction {
  private final VisualPrimitive.PrimitiveCursor cursor;

  public PrimitiveActionPaste(History history, VisualPrimitive.PrimitiveCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    final String text = context.uncopyString();
    if (text == null) return false;
    ValuePrimitive value = cursor.visualPrimitive.value;
    if (cursor.range.beginOffset != cursor.range.endOffset)
      history.apply(
          context,
          new ChangePrimitiveRemove(
              value, cursor.range.beginOffset, cursor.range.endOffset - cursor.range.beginOffset));
    history.apply(context, new ChangePrimitiveAdd(value, cursor.range.beginOffset, text));
    return true;
  }
}
