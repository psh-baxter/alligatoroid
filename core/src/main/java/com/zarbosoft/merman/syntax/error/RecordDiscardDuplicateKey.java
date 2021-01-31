package com.zarbosoft.merman.syntax.error;

public class RecordDiscardDuplicateKey extends BaseKVError {
  public RecordDiscardDuplicateKey(String key) {
    put("key", key);
  }

  @Override
  protected String name() {
    return "discard key duplicates existing pair";
  }
}
