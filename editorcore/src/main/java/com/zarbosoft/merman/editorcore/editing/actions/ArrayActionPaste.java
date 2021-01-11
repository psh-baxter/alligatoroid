package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

import java.util.List;

public class ArrayActionPaste extends EditAction {
  private final VisualArray.ArrayCursor cursor;

  public ArrayActionPaste(History history, VisualArray.ArrayCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    final List<Atom> atoms = context.uncopy(cursor.self.value.back().elementAtomType());
    if (atoms.isEmpty()) return false;
    history.apply(
        context,
        new ChangeArray(
            cursor.self.value, cursor.beginIndex, cursor.endIndex - cursor.beginIndex + 1, atoms));
    return true;
  }
}
