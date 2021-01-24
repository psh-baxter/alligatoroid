package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BackSpec;

public class RecordChildNotValueAt extends BaseKVError{

  public RecordChildNotValueAt(
          Path typePath,
          AtomType candidate,
          int childIndex,
          BackSpec child
  ) {
        put("typePath", typePath);
        put("candidate", candidate);
        put("childIndex", childIndex);
        put("child", child);
  }

  @Override
  protected String name() {
    return "record element candidate back field should be value but was key";
  }
}
