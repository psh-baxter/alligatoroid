package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.visual.Vector;

public abstract class MockeryCourseDisplayNode extends MockeryDisplayNode
    implements CourseDisplayNode {
  public double baselineTransverse;

  @Override
  public double transverse() {
    return baselineTransverse - ascent();
  }

  @Override
  public double baselineTransverse() {
    return baselineTransverse;
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    this.converse = vector.converse;
    this.baselineTransverse = vector.transverse;
  }

  @Override
  public void setBaselineTransverse(final double baseline, final boolean animate) {
    this.baselineTransverse = baseline;
  }
}
