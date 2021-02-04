package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Vector;

public interface DisplayNode {
  int converse();

  int transverse();

  int converseSpan();

  int transverseSpan();

  void setConverse(int converse, boolean animate);

  default void setConverse(final int converse) {
    setConverse(converse, false);
  }

  void setTransverse(int transverse, boolean animate);

  default void setTransverse(final int transverse) {
    setTransverse(transverse, false);
  }

  void setPosition(final Vector vector, final boolean animate);

  default int converseEdge() {
    return converse() + converseSpan();
  }

  default int transverseEdge() {
    return transverse() + transverseSpan();
  }
}
