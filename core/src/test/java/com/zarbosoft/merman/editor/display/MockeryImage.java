package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public class MockeryImage extends MockeryCourseDisplayNode implements Image {
  @Override
  public void setImage(final Context context, final String path) {}

  @Override
  public void rotate(final Context context, final double rotate) {}

  @Override
  public double converseSpan() {
    return 25;
  }

  @Override
  public double ascent() {
    return 25;
  }

  @Override
  public double descent() {
    return 0;
  }
}
