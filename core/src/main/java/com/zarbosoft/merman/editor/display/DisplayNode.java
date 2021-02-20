package com.zarbosoft.merman.editor.display;

public interface DisplayNode {
  int converse();

  int transverse();

  int transverseSpan();

  default int transverseEdge() {
    return transverse() + transverseSpan();
  }

  int converseSpan();

  void setConverse(int converse, boolean animate);

  default void setConverse(final int converse) {
    setConverse(converse, false);
  }

  default int converseEdge() {
    return converse() + converseSpan();
  }
}
