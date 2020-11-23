package com.zarbosoft.merman.editor.visual.tags;

import java.util.Objects;

public class FreeTag implements Tag {

  public String value;

  public FreeTag() {}

  public FreeTag(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(FreeTag.class.hashCode(), value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof FreeTag && value.equals(((FreeTag) obj).value);
  }

  public String toString() {
    return String.format("free:%s", value);
  }
}
