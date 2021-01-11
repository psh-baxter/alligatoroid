package com.zarbosoft.merman.editorcore.syntaxgap.actions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.syntaxgap.SyntacticGapChoicesExtension;

@Action.StaticID(id = "insert_after")
public class ArrayActionInsertAfter extends EditAction {
  private final VisualArray.ArrayCursor cursor;

  public ArrayActionInsertAfter(History history, VisualArray.ArrayCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    final Atom created =
        SyntacticGapChoicesExtension.arrayInsertNewDefault(
            context, history, cursor.self.value, cursor.endIndex + 1);
    if (!created.visual.selectDown(context)) cursor.setPosition(context, cursor.endIndex + 1);
    return true;
  }
}
