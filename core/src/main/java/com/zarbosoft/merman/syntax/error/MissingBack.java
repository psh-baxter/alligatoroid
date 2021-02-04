package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;

public class MissingBack extends BaseKVError{

  public MissingBack(Path typePath, String field) {
        put("typePath", typePath);
        put("field", field);
  }

  @Override
  protected String description() {
    return "missing back field";
  }
}
