package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class TypeInvalidAtLocation extends BaseKVError{
  public TypeInvalidAtLocation(SyntaxPath typePath) {
        put("backPath", typePath);
  }

  @Override
  protected String description() {
    return "type directly within type (invalid luxem)";
  }
}
