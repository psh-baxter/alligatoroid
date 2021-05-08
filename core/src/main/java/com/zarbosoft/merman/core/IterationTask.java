package com.zarbosoft.merman.core;

public abstract class IterationTask implements Comparable<IterationTask> {
	public static class P {
		public final static double coursePlace = 170;
		public final static double courseCompact = 165;
		public final static double wallAdjust = 160;
		public final static double layBricks = 150;
		public final static double notifyBricks = 140;
		public final static double wallCompact = 110;
		public final static double courseExpand = -95;
		public final static double wallExpand = -100;
	}

	public boolean destroyed = false;

	protected double priority() {
		return 0;
	}

	protected abstract boolean runImplementation(IterationContext iterationContext);

	public boolean run(final IterationContext iterationContext) {
		if (destroyed)
			return false;
		final boolean out = runImplementation(iterationContext);
		if (!out)
			destroy();
		return out;
	}

	@Override
	public int compareTo(final IterationTask t) {
		return -Double.compare(priority(), t.priority());
	}

	public void destroy() {
		if (destroyed)
			throw new AssertionError();
		destroyed();
		destroyed = true;
	}

	protected abstract void destroyed();

}
