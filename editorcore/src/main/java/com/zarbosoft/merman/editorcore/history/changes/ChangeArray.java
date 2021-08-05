package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.Change;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

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
  public Change apply(final Editor editor) {
    final TSList<Atom> clearSublist = value.data.sublist(index, index + remove);
    final ChangeArray reverse = new ChangeArray(value, index, add.size(), clearSublist.mut());
    for (Atom atom : clearSublist) {
      atom.setFieldParentRef(null);
    }
    clearSublist.clear();
    value.data.insertAll(index, add);
    for (Atom atom : add) {
      atom.setFieldParentRef(new FieldArray.Parent(value));
    }
    value.renumber(index);
    for (final FieldArray.Listener listener : value.listeners) {
      listener.changed(editor.context, index, remove, add);
    }
    return reverse;
  }
}
