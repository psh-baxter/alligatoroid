package com.zarbosoft.merman.core.display;

import com.zarbosoft.merman.core.Context;

public interface Image extends CourseDisplayNode {
  void setImage(Context context, String path);

  void rotate(Context context, double rotate);
}
