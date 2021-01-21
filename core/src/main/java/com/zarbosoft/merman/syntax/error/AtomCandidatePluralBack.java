package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;

public class AtomCandidatePluralBack extends BaseKVError{
  public AtomCandidatePluralBack(Path typePath, AtomType child, int childBackSize) {
      put("typePath", typePath);
      put("child", child);
      put("childBackSize", childBackSize);
  }

  @Override
  protected String name() {
    return "atom candidate serializes to multiple values in single value context";
  }
}
