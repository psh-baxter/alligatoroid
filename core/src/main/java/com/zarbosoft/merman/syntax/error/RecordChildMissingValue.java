package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;

public class RecordChildMissingValue extends BaseKVError{
  public RecordChildMissingValue(
          Path typePath, AtomType candidate) {
        put("typePath", typePath);
        put("candidate", candidate);
  }

  @Override
  protected String description() {
    return "record element candidate has key but no value";
  }
}
