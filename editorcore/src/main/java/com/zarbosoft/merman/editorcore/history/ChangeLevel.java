package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SelectionState;
import com.zarbosoft.rendaw.common.TSList;

public class ChangeLevel extends Change {
  public final TSList<Change> subchanges = new TSList<>();
  final int id;
  SelectionState select;

  ChangeLevel(final int id) {
    this.id = id;
  }

  @Override
  public boolean merge(final Change other) {
    if (subchanges.isEmpty()) {
      subchanges.add(other);
    } else if (subchanges.last().merge(other)) {
    } else subchanges.add(other);
    return true;
  }

  @Override
  public Change apply(final Context context) {
    final ChangeLevel out = new ChangeLevel(id);
    out.select = context.cursor.saveState();
    for (int i = 0; i < subchanges.size(); ++i) {
      Change change = subchanges.getRev(i);
      out.subchanges.add(change.apply(context));
    }
    if (select != null) select.select(context);
    return out;
  }

  public boolean isEmpty() {
    return subchanges.isEmpty();
  }
}
