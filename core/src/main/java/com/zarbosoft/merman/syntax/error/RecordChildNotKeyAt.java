package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BackSpec;

public class RecordChildNotKeyAt extends BaseKVError {

  public RecordChildNotKeyAt(
          Path typePath, AtomType candidate, int childIndex, BackSpec child) {
            put("typePath", typePath);
            put("candidate", candidate);
            put("childIndex", childIndex);
            put("child", child);
  }

  @Override
  protected String description() {
    return "record element candidate back field should be key but was value";
  }
}
