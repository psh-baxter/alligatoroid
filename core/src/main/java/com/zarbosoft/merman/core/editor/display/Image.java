package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.editor.Context;

public interface Image extends CourseDisplayNode {
  void setImage(Context context, String path);

  void rotate(Context context, double rotate);
}
