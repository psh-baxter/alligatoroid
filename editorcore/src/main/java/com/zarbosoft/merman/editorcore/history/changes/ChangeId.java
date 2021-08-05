package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.core.document.fields.FieldId;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.Change;

public class ChangeId extends Change {
  private final FieldId value;
  private int newId;

  public ChangeId(final FieldId value, int newId) {
    this.value = value;
    this.newId = newId;
  }

  @Override
  public boolean merge(final Change other) {
    final ChangeId other2;
    try {
      other2 = (ChangeId) other;
    } catch (final ClassCastException e) {
      return false;
    }
    if (other2.value != value) return false;
    newId = other2.newId;
    return true;
  }

  @Override
  public Change apply(final Editor editor) {
    final ChangeId reverse = new ChangeId(value, value.id);
    value.id = newId;
    for (final FieldId.Listener listener : value.listeners) {
      listener.changed(editor.context, newId);
    }
    return reverse;
  }
}
