package com.zarbosoft.merman.core;

public abstract class IterationTask implements Comparable<IterationTask> {
  public boolean destroyed = false;

  protected double priority() {
    return 0;
  }

  protected abstract boolean runImplementation(IterationContext iterationContext);

  public boolean run(final IterationContext iterationContext) {
    if (destroyed) return false;
    final boolean out = runImplementation(iterationContext);
    if (!out) destroy();
    return out;
  }

  @Override
  public int compareTo(final IterationTask t) {
    return -Double.compare(priority(), t.priority());
  }

  public void destroy() {
    if (destroyed) throw new AssertionError();
    destroyed();
    destroyed = true;
  }

  protected abstract void destroyed();

  public static class P {
    public static final double coursePlace = 170;
    public static final double courseCompact = 165;
    public static final double wallAdjust = 160;
    public static final double layBricks = 150;
    public static final double notifyBricks = 140;
    public static final double wallCompact = 110;
    public static final double courseExpand = -95;
    public static final double wallExpand = -100;
  }
}
