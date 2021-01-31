package com.zarbosoft.merman.editor.visual.alignment;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.rendaw.common.ROMap;

public class RelativeAlignment extends Alignment implements AlignmentListener {
	private final String baseKey;
	private final int offset;
	private Alignment base;

	public RelativeAlignment(final String baseKey, final int offset) {
		this.baseKey = baseKey;
		this.offset = offset;
		converse = offset;
	}

	@Override
	public void feedback(final Context context, final int position) {

	}

	@Override
	public void root(final Context context, final ROMap<String, Alignment> parents) {
		if (base != null) {
			base.removeListener(context, this);
		}
		base = parents.getOpt(baseKey);
		if (base == this)
			throw new AssertionError("Alignment parented to self");
		if (base != null)
			base.addListener(context, this);
		align(context);
	}

	@Override
	public void destroy(final Context context) {

	}

	@Override
	public void align(final Context context) {
		converse = (base == null ? 0 : base.converse) + offset;
		submit(context);
	}

	@Override
	public int getConverseLowerBound(final Context context) {
		return converse;
	}

	@Override
	public String toString() {
		return String.format("relative-%d-p-%s", converse, base);
	}
}
