package com.zarbosoft.merman.editor.visual.alignment;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.rendaw.common.ROMap;

public class AbsoluteAlignment extends Alignment implements AlignmentListener {
	public AbsoluteAlignment(final int offset) {
		converse = offset;
	}

	@Override
	public void feedback(final Context context, final int position) {

	}

	@Override
	public void root(final Context context, final ROMap<String, Alignment> parents) {
		align(context);
	}

	@Override
	public void destroy(final Context context) {

	}

	@Override
	public void align(final Context context) {
		submit(context);
	}

	@Override
	public int getConverseLowerBound(final Context context) {
		return converse;
	}

	@Override
	public String toString() {
		return String.format("absolute-%s", converse);
	}
}
