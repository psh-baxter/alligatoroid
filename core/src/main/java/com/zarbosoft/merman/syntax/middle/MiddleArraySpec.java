package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;

public class MiddleArraySpec extends MiddleArraySpecBase {
  @Override
  public Path getPath(final ValueArray value, final int actualIndex) {
    return value.getPath().add(String.format("%d", actualIndex));
  }
}
