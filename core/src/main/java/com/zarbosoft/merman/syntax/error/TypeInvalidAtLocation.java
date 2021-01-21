package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;

public class TypeInvalidAtLocation extends BaseKVError{
  public TypeInvalidAtLocation(Path typePath) {
        put("backPath", typePath);
  }

  @Override
  protected String name() {
    return "type directly within type (invalid luxem)";
  }
}
