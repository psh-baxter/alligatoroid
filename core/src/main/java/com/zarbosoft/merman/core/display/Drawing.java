package com.zarbosoft.merman.core.display;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.visual.Vector;

public interface Drawing extends FreeDisplayNode {
  void clear();

  void resize(Context context, Vector vector);

  DrawingContext begin(Context context);
}
