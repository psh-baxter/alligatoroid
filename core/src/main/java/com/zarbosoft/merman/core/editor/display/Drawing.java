package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.visual.Vector;

public interface Drawing extends FreeDisplayNode {
  void clear();

  void resize(Context context, Vector vector);

  DrawingContext begin(Context context);
}
