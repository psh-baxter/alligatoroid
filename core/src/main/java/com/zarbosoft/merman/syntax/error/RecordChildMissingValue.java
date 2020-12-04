package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.back.BackRecordSpec;

public class RecordChildMissingValue extends BaseKVError{
  public RecordChildMissingValue(
      Path typePath, FreeAtomType candidate) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("typePath", typePath)
        .put("candidate", candidate)
        .build());
  }

  @Override
  protected String name() {
    return "record element candidate has key but no value";
  }
}
