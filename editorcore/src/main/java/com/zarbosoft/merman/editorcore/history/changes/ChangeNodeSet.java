package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.editorcore.history.Change;

public class ChangeNodeSet extends Change {
	private final FieldAtom value;
	private Atom atom;

	public ChangeNodeSet(final FieldAtom value, final Atom newValue) {
		this.value = value;
		atom = newValue;
	}

	@Override
	public boolean merge(final Change other) {
		final ChangeNodeSet other2;
		try {
			other2 = (ChangeNodeSet) other;
		} catch (final ClassCastException e) {
			return false;
		}
		if (other2.value != value)
			return false;
		atom = other2.atom;
		return true;
	}

	@Override
	public Change apply(final Context context) {
		final Change reverse = new ChangeNodeSet(value, value.data);
		value.data.setValueParentRef(null);
		value.data = atom;
		atom.setValueParentRef(new FieldAtom.Parent(value));
		for (final FieldAtom.Listener listener : value.listeners)
			listener.set(context, atom);
		return reverse;
	}
}
