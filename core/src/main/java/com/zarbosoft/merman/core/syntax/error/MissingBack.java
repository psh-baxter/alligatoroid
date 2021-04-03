package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class MissingBack extends BaseKVError{

  public MissingBack(SyntaxPath typePath, String field) {
        put("typePath", typePath);
        put("field", field);
  }

  @Override
  protected String description() {
    return "missing back field";
  }
}
