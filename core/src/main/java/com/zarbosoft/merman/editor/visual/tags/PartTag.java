package com.zarbosoft.merman.editor.visual.tags;

import java.util.Objects;

public class PartTag implements Tag {

  public String value;

  public PartTag() {}

  public PartTag(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PartTag.class.hashCode(), value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof PartTag && value.equals(((PartTag) obj).value);
  }

  public String toString() {
    return String.format("part:%s", value);
  }
}
