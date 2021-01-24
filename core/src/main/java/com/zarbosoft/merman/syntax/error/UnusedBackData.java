package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.rendaw.common.ROSet;

public class UnusedBackData extends BaseKVError {

  public UnusedBackData(ROSet<String> unused) {
    put("unused", unused);
  }

  @Override
  protected String name() {
    return "unused data from back fields";
  }
}
