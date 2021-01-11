package com.zarbosoft.merman.editorcore.syntaxgap.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

@Action.StaticID(id = "suffix")
public
class ArrayActionSuffix extends Action {
  private final VisualArray.ArrayCursor cursor;
  private final History history;

  public ArrayActionSuffix(History history, VisualArray.ArrayCursor cursor) {
    this.cursor = cursor;
    this.history = history;
  }

  @Override
  public boolean run(final Context context) {
    final Atom gap = context.syntax.suffixGap.create(false, cursor.self.value.data.get(index));
    history.apply(
        context,
        new ChangeArray(
            cursor.self.value,
            cursor.beginIndex,
            cursor.endIndex - cursor.beginIndex,
            ImmutableList.of(gap)));
    gap.fields.getOpt("gap").selectDown(context);
    return true;
  }
}
