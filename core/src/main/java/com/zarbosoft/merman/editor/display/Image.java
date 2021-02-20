package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public interface Image extends CourseDisplayNode {
  void setImage(Context context, String path);

  void rotate(Context context, double rotate);
}
