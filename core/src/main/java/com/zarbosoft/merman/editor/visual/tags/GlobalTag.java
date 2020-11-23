package com.zarbosoft.merman.editor.visual.tags;

import java.util.Objects;

public class GlobalTag implements Tag {

  public String value;

  public GlobalTag() {}

  public GlobalTag(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GlobalTag.class.hashCode(), value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof GlobalTag && value.equals(((GlobalTag) obj).value);
  }

  public String toString() {
    return String.format("global:%s", value);
  }
}
