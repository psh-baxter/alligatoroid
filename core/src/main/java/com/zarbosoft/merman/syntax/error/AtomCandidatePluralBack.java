package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.FreeAtomType;

public class AtomCandidatePluralBack extends BaseKVError{
  public AtomCandidatePluralBack(Path typePath, FreeAtomType child, int childBackSize) {
    super(ImmutableMap.<String, Object>builder()
      .put("typePath", typePath)
      .put("child", child)
      .put("childBackSize", childBackSize)
      .build());
  }

  @Override
  protected String name() {
    return "atom candidate serializes to multiple values in single value context";
  }
}
