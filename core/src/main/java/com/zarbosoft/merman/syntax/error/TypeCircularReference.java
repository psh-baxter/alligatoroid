package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.rendaw.common.ROList;

public class TypeCircularReference extends BaseKVError{

  public TypeCircularReference(ROList<String> subpath) {
        put("subpath", subpath);
  }

  @Override
  protected String description() {
    return "type circular reference";
  }
}
