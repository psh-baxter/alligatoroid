package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.rendaw.common.ROSet;

public class UnusedBackData extends BaseKVError {

  public UnusedBackData(ROSet<String> unused) {
    put("unused", unused.inner_());
  }

  @Override
  protected String description() {
    return "unused data from back fields";
  }
}
