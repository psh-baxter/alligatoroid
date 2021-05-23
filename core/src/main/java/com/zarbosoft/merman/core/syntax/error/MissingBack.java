package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class MissingBack extends BaseKVError{

  public MissingBack(SyntaxPath typePath, String field, String forName) {
        put("typePath", typePath);
        put("field", field);
        put("for", forName);
  }

  @Override
  protected String description() {
    return "missing back field";
  }
}
