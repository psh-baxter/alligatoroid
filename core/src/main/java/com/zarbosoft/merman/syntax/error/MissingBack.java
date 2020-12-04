package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;

public class MissingBack extends BaseKVError{

  public MissingBack(Path typePath, String field) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("typePath", typePath)
        .put("field", field)
        .build());
  }

  @Override
  protected String name() {
    return "missing back field";
  }
}
