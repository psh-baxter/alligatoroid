package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.AtomType;

public class AtomCandidatePluralBack extends BaseKVError{
  public AtomCandidatePluralBack(SyntaxPath typePath, AtomType child, int childBackSize) {
      put("typePath", typePath);
      put("child", child);
      put("childBackSize", childBackSize);
  }

  @Override
  protected String description() {
    return "atom candidate serializes to multiple values in single value context";
  }
}
