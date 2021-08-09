package com.zarbosoft.luxem.read.path;

public class LuxemRecordPath extends LuxemPath {

  private String key;

  public LuxemRecordPath(final LuxemPath parent) {
    this.parent = parent;
  }

  public LuxemRecordPath(final LuxemPath parent, final String key) {
    this.parent = parent;
    this.key = key;
  }

  @Override
  public LuxemPath unkey() {
    return new LuxemRecordPath(parent, null);
  }

  @Override
  public LuxemPath value() {
    return this;
  }

  @Override
  public LuxemPath key(final String data) {
    return new LuxemRecordPath(parent, data);
  }

  @Override
  public LuxemPath type() {
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s/%s", parent == null ? "" : parent.toString(), key == null ? "" : key);
  }
}
