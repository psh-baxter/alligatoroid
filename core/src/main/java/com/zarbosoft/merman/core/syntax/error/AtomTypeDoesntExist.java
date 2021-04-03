package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class AtomTypeDoesntExist extends BaseKVError{

  public AtomTypeDoesntExist(SyntaxPath typePath, String type) {
      put("typePath", typePath);
      put("candidateType", type);
  }

  @Override
  protected String description() {
    return "specified candidate type doesn't exist";
  }
}
