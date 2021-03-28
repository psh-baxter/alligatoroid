package com.zarbosoft.merman.core.syntax.error;

public class RecordDiscardDuplicateKey extends BaseKVError {
  public RecordDiscardDuplicateKey(String key) {
    put("key", key);
  }

  @Override
  protected String description() {
    return "discard key duplicates existing pair";
  }
}
