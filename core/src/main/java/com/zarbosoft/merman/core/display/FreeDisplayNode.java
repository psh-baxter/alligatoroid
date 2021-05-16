package com.zarbosoft.merman.core.display;

import com.zarbosoft.merman.core.visual.Vector;

public interface FreeDisplayNode extends DisplayNode {
  void setPosition(Vector vector, boolean animate);
}
