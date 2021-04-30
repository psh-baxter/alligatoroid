package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.editorcore.history.Change;

public class ChangePrimitive extends Change {
  private final FieldPrimitive value;
  private String add;
  private int index;
  private int remove;

  public ChangePrimitive(
      final FieldPrimitive value, final int index, final int remove, final String add) {
    this.value = value;
    this.index = index;
    this.remove = remove;
    this.add = add;
  }

  @Override
  public boolean merge(final Change other) {
    final ChangePrimitive other2;
    try {
      other2 = (ChangePrimitive) other;
    } catch (final ClassCastException e) {
      return false;
    }
    if (other2.value != value) return false;
    if (other2.index + other2.remove == index) {
      index = other2.index;
      remove += other2.remove;
      add = other2.add + add;
    } else if (index + remove == other2.index) {
      remove += other2.remove;
      add += other2.add;
    } else return false;
    return true;
  }

  @Override
  public Change apply(final Context context) {
    final String clearSublist = value.data.substring(index, index + remove);
    final ChangePrimitive reverse = new ChangePrimitive(value, index, add.length(), clearSublist);
    value.data.replace(index, index + remove, "");
    value.data.insert(index, add);
    for (final FieldPrimitive.Listener listener : value.listeners) {
      listener.changed(context, index, remove, add);
    }
    return reverse;
  }
}
