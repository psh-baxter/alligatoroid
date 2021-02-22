package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.visual.Vector;

public interface CourseDisplayNode extends FreeDisplayNode {

  void setBaselineTransverse(double transverse, boolean animate);

  default void setBaselineTransverse(final double transverse) {
    setBaselineTransverse(transverse, false);
  }

  void setBaselinePosition(final Vector vector, final boolean animate);

  double baselineTransverse();

  double ascent();

  double descent();

  default double transverseEdge() {
    return baselineTransverse() + descent();
  }

  default double transverse() {
    return baselineTransverse() - ascent();
  }

  default double transverseSpan() {
    return ascent() + descent();
  }

  default void setPosition(Vector vector, boolean animate) {
    setBaselinePosition(new Vector(vector.converse, vector.transverse - ascent()), animate);
  }
}
