package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

import java.util.List;

public class ArrayActionMoveAfter extends EditAction {
    public String id() {
        return "move_after";
    }
  private final VisualFrontArray.ArrayCursor cursor;

  public ArrayActionMoveAfter(EditingExtension edit, VisualFrontArray.ArrayCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    if (cursor.endIndex == cursor.visual.value.data.size() - 1) return false;
    int index = cursor.beginIndex;
    final List<Atom> atoms =
        ImmutableList.copyOf(cursor.visual.value.data.subList(index, cursor.endIndex + 1));
    edit.history.apply(
        context, new ChangeArray(cursor.visual.value, index, atoms.size(), ImmutableList.of()));
    cursor.setPosition(context, ++index);
    edit.history.apply(context, new ChangeArray(cursor.visual.value, index, 0, atoms));
    cursor.leadFirst = false;
    cursor.setRange(context, index, index + atoms.size() - 1);
    return true;
  }
}
