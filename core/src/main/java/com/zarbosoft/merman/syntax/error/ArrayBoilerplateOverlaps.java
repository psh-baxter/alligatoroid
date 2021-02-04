package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;

public class ArrayBoilerplateOverlaps extends BaseKVError {
  public ArrayBoilerplateOverlaps(
      Path typePath, String boilerplate, AtomType splayedType, String otherBoilerplate) {
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
