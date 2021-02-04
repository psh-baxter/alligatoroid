package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;

public class AtomTypeDoesntExist extends BaseKVError{

  public AtomTypeDoesntExist(Path typePath, String type) {
      put("typePath", typePath);
      put("candidateType", type);
  }

  @Override
  protected String description() {
    return "specified candidate type doesn't exist";
  }
}
