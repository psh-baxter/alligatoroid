package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.visual.Vector;

public abstract class MockeryCourseDisplayNode extends MockeryDisplayNode
    implements CourseDisplayNode {
  public int baselineTransverse;

  @Override
  public int transverse() {
    return baselineTransverse - ascent();
  }

  @Override
  public int baselineTransverse() {
    return baselineTransverse;
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    this.converse = vector.converse;
    this.baselineTransverse = vector.transverse;
  }

  @Override
  public void setBaselineTransverse(final int baseline, final boolean animate) {
    this.baselineTransverse = baseline;
  }
}
