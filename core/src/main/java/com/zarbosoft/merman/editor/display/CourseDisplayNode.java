package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.visual.Vector;

public interface CourseDisplayNode extends FreeDisplayNode {

  void setBaselineTransverse(int transverse, boolean animate);

  default void setBaselineTransverse(final int transverse) {
    setBaselineTransverse(transverse, false);
  }

  void setBaselinePosition(final Vector vector, final boolean animate);

  int baselineTransverse();

  int ascent();

  int descent();

  default int transverseEdge() {
    return baselineTransverse() + descent();
  }

  default int transverse() {
    return baselineTransverse() - ascent();
  }

  default int transverseSpan() {
    return ascent() + descent();
  }

  default void setPosition(Vector vector, boolean animate) {
    setBaselinePosition(new Vector(vector.converse, vector.transverse - ascent()), animate);
  }
}
