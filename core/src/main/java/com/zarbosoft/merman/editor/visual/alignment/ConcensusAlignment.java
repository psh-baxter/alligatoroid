package com.zarbosoft.merman.editor.visual.alignment;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSSet;

public class ConcensusAlignment extends Alignment {
	private boolean disabled = false;
	/**
	 * Alignments that have appeared before this alignment on any course
	 */
	public TSSet<ConcensusAlignment> superior = new TSSet<>();
	private IterationAlign iterationAlign;

	private class IterationAlign extends IterationTask {
		private final Context context;

		private IterationAlign(final Context context) {
			this.context = context;
		}

		@Override
		protected boolean runImplementation(final IterationContext iterationContext) {
			final int oldConverse = converse;
			converse = disabled ?
					0 :
					listeners.stream().mapToInt(listeners -> listeners.getConverseLowerBound(context)).max().orElse(0);
			if (oldConverse != converse)
				listeners.stream().forEach(listener -> listener.align(context));
			return false;
		}

		@Override
		protected void destroyed() {
			iterationAlign = null;
		}
	}

	private void iterationAlign(final Context context) {
		if (iterationAlign == null) {
			iterationAlign = new IterationAlign(context);
			context.addIteration(iterationAlign);
		}
	}

	@Override
	public void destroy(final Context context) {
		if (iterationAlign != null)
			iterationAlign.destroy();
	}

	@Override
	public void removeListener(final Context context, final AlignmentListener listener) {
		super.removeListener(context, listener);
		if (listener.getConverseLowerBound(context) == converse)
			iterationAlign(context);
	}

	@Override
	public void feedback(final Context context, final int gotConverse) {
		if (disabled)
			throw new AssertionError();
		iterationAlign(context);
	}

	@Override
	public void root(final Context context, final ROMap<String, Alignment> parents) {
	}

	public void disable(final Context context) {
		disabled = true;
		iterationAlign(context);
	}

}
