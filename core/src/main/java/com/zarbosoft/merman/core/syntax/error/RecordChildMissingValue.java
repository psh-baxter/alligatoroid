package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.AtomType;

public class RecordChildMissingValue extends BaseKVError{
  public RecordChildMissingValue(
          SyntaxPath typePath, AtomType candidate) {
        put("typePath", typePath);
        put("candidate", candidate);
  }

  @Override
  protected String description() {
    return "record element candidate has key but no value";
  }
}
