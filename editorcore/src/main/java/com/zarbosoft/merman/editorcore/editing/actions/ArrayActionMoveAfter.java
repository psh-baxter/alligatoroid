package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

import java.util.List;

@Action.StaticID(id = "move_after")
public class ArrayActionMoveAfter extends EditAction {
  private final VisualArray.ArrayCursor cursor;

  public ArrayActionMoveAfter(History history, VisualArray.ArrayCursor cursor) {
    super(history);
    this.cursor = cursor;
  }

  @Override
  public boolean run1(final Context context) {
    if (cursor.endIndex == cursor.self.value.data.size() - 1) return false;
    int index = cursor.beginIndex;
    final List<Atom> atoms =
        ImmutableList.copyOf(cursor.self.value.data.subList(index, cursor.endIndex + 1));
    history.apply(
        context, new ChangeArray(cursor.self.value, index, atoms.size(), ImmutableList.of()));
    cursor.setPosition(context, ++index);
    history.apply(context, new ChangeArray(cursor.self.value, index, 0, atoms));
    cursor.leadFirst = false;
    cursor.setRange(context, index, index + atoms.size() - 1);
    return true;
  }
}
