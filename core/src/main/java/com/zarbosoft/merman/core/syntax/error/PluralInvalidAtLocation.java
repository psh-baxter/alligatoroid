package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class PluralInvalidAtLocation extends BaseKVError{

  public PluralInvalidAtLocation(SyntaxPath typePath) {
        put("typePath", typePath);
  }

  @Override
  protected String description() {
    return "back field must describe single element (not subarray) here";
  }
}
