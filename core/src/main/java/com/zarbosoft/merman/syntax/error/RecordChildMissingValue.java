package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;

public class RecordChildMissingValue extends BaseKVError{
  public RecordChildMissingValue(
          Path typePath, AtomType candidate) {
        put("typePath", typePath);
        put("candidate", candidate);
  }

  @Override
  protected String name() {
    return "record element candidate has key but no value";
  }
}
