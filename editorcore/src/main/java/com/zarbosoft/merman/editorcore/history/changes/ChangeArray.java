package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.history.Change;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.List;

public class ChangeArray extends Change {

  private final FieldArray value;
  private final TSList<Atom> add = new TSList<>();
  private int index;
  private int remove;

  public ChangeArray(
          final FieldArray value, final int index, final int remove, final ROList<Atom> add) {
    this.value = value;
    this.index = index;
    this.remove = remove;
    this.add.addAll(add);
  }

  @Override
  public boolean merge(final Change other) {
    final ChangeArray other2;
    try {
      other2 = (ChangeArray) other;
    } catch (final ClassCastException e) {
      return false;
    }
    if (other2.value != value) return false;
    if (other2.index + other2.remove == index) {
      index = other2.index;
      remove += other2.remove;
      add.insertAll(0, other2.add);
    } else if (index + remove == other2.index) {
      remove += other2.remove;
      add.addAll(other2.add);
    } else return false;
    return true;
  }

  @Override
  public Change apply(final Context context) {
    final List<Atom> clearSublist = value.data.subList(index, index + remove);
    final ChangeArray reverse =
        new ChangeArray(value, index, add.size(), ImmutableList.copyOf(clearSublist));
    clearSublist.stream().forEach(v -> v.setValueParentRef(null));
    clearSublist.clear();
    value.data.insertAll(index, add);
    add.stream()
        .forEach(
            v -> {
              v.setValueParentRef(new FieldArray.ArrayParent(value));
            });
    value.renumber(index);
    for (final FieldArray.Listener listener : value.listeners) {
      listener.changed(context, index, remove, add);
    }
    return reverse;
  }
}
