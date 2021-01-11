package com.zarbosoft.merman.editorcore.syntaxgap.actions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;

@Action.StaticID(id = "insert_before")
public class ArrayActionInsertBefore extends EditAction {
  private final VisualArray.ArrayCursor cursor;

  public ArrayActionInsertBefore(History history, VisualArray.ArrayCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    final Atom created =
        EditingExtension.arrayInsertNewDefault(
            context, history, cursor.self.value, cursor.beginIndex);
    if (!created.visual.selectDown(context)) cursor.setPosition(context, cursor.beginIndex);
    return true;
  }
}
