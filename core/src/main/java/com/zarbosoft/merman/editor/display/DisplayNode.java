package com.zarbosoft.merman.editor.display;

public interface DisplayNode {
  double converse();

  double transverse();

  double transverseSpan();

  default double transverseEdge() {
    return transverse() + transverseSpan();
  }

  double converseSpan();

  void setConverse(double converse, boolean animate);

  default void setConverse(final double converse) {
    setConverse(converse, false);
  }

  default double converseEdge() {
    return converse() + converseSpan();
  }
}
