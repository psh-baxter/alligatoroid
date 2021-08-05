package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.rendaw.common.TSList;

public class ChangeLevel  {
  public final TSList<Change> subchanges = new TSList<>();
  CursorState select;
  public final long unique;

  ChangeLevel(long unique) {
    this.unique = unique;
  }

  public boolean merge(final Change other) {
    if (subchanges.isEmpty()) {
      subchanges.add(other);
    } else if (subchanges.last().merge(other)) {
    } else subchanges.add(other);
    return true;
  }

  public ChangeLevel apply(final Editor editor) {
    final ChangeLevel out = new ChangeLevel(unique);
    if (editor.context.cursor != null) out.select = editor.context.cursor.saveState();
    for (int i = 0; i < subchanges.size(); ++i) {
      Change change = subchanges.getRev(i);
      out.subchanges.add(change.apply(editor));
    }
    if (select != null) select.select(editor.context);
    return out;
  }

  public boolean isEmpty() {
    return subchanges.isEmpty();
  }
}
