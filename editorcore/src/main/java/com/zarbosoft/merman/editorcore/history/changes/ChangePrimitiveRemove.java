package com.zarbosoft.merman.editorcore.history.changes;

import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.history.Change;

public class ChangePrimitiveRemove extends Change {

	private final FieldPrimitive data;
	private int index;
	private int size;

	public ChangePrimitiveRemove(final FieldPrimitive data, final int index, final int size) {
		this.data = data;
		this.index = index;
		this.size = size;
	}

	@Override
	public boolean merge(final Change other) {
		if (!(other instanceof ChangePrimitiveRemove))
			return false;
		final ChangePrimitiveRemove other2 = (ChangePrimitiveRemove) other;
		if (other2.data != data)
			return false;
		if (other2.index + other2.size < index)
			return false;
		if (other2.index > index + size)
			return false;
		if (other2.index < other2.size) {
			index = other2.index;
			size += other2.size;
		} else {
			size += other2.size;
		}
		return true;
	}

	@Override
	public Change apply(final Context context) {
		final ChangePrimitiveAdd reverse =
				new ChangePrimitiveAdd(data, index, data.data.substring(index, index + size));
		data.data.delete(index, index + size);
		for (final FieldPrimitive.Listener listener : data.listeners)
			listener.removed(context, index, size);
		return reverse;
	}
}
