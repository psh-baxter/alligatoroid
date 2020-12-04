package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;

public class PluralInvalidAtLocation extends BaseKVError{

  public PluralInvalidAtLocation(Path typePath) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("typePath", typePath)
        .build());
  }

  @Override
  protected String name() {
    return "back field must describe single element (not subarray) here";
  }
}
