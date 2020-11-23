package com.zarbosoft.merman.editor.visual.tags;

import java.util.Objects;

public class TypeTag implements Tag {

  public String value;

  public TypeTag() {}

  public TypeTag(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(TypeTag.class.hashCode(), value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof TypeTag && value.equals(((TypeTag) obj).value);
  }

  public String toString() {
    return String.format("type:%s", value);
  }
}
