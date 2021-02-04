package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackSpecData;

public class BackFieldWrongType extends BaseKVError {
  public BackFieldWrongType(Path typePath, String field, BackSpecData found, Class expected) {
            put("typePath", typePath);
            put("field", field);
            put("found", found);
            put("expected", expected);
  }

  @Override
  protected String description() {
    return "back field has incompatible type";
  }
}
