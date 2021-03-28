package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.editor.Path;

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
