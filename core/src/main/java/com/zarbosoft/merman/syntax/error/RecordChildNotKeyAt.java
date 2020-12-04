package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.back.BackSpec;

public class RecordChildNotKeyAt extends BaseKVError {

  public RecordChildNotKeyAt(
      Path typePath, FreeAtomType candidate, int childIndex, BackSpec child) {
    super(
        ImmutableMap.<String, Object>builder()
            .put("typePath", typePath)
            .put("candidate", candidate)
            .put("childIndex", childIndex)
            .put("child", child)
            .build());
  }

  @Override
  protected String name() {
    return "record element candidate back field should be key but was value";
  }
}
