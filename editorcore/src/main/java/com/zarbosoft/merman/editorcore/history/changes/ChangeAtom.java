package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.Change;

public class ChangeAtom extends Change {
  private final FieldAtom value;
  private Atom atom;

  public ChangeAtom(final FieldAtom value, final Atom newValue) {
    this.value = value;
    atom = newValue;
  }

  @Override
  public boolean merge(final Change other) {
    final ChangeAtom other2;
    try {
      other2 = (ChangeAtom) other;
    } catch (final ClassCastException e) {
      return false;
    }
    if (other2.value != value) return false;
    atom = other2.atom;
    return true;
  }

  @Override
  public Change apply(final Editor editor) {
    final Change reverse = new ChangeAtom(value, value.data);
    if (value.data != null) /* modifying new fields, not yet in tree */
      value.data.setFieldParentRef(null);
    value.data = atom;
    if (atom != null) /* undoing initial creation, might be null */
      atom.setFieldParentRef(new FieldAtom.Parent(value));
    for (final FieldAtom.Listener listener : value.listeners) listener.set(editor.context, atom);
    return reverse;
  }
}
