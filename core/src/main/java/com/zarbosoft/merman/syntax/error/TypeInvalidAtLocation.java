package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;

public class TypeInvalidAtLocation extends BaseKVError{
  public TypeInvalidAtLocation(Path typePath) {
        put("backPath", typePath);
  }

  @Override
  protected String description() {
    return "type directly within type (invalid luxem)";
  }
}
