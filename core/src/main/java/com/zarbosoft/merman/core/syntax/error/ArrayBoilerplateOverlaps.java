package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.AtomType;

public class ArrayBoilerplateOverlaps extends BaseKVError {
  public ArrayBoilerplateOverlaps(
          SyntaxPath typePath, String boilerplate, AtomType splayedType, String otherBoilerplate) {
    put("typePath", typePath);
    put("boilerplate", boilerplate);
    put("splayedType", splayedType);
    put("otherBoilerplate", otherBoilerplate);
  }

  @Override
  protected String description() {
    return "boilerplate type overlaps other boilerplate type";
  }
}
