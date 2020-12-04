package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackSpecData;

public class BackFieldWrongType extends BaseKVError {
  public BackFieldWrongType(Path typePath, String field, BackSpecData found, Class expected) {
    super(
        ImmutableMap.<String, Object>builder()
            .put("typePath", typePath)
            .put("field", field)
            .put("found", found)
            .put("expected", expected)
            .build());
  }

  @Override
  protected String name() {
    return "back field has incompatible type";
  }
}
