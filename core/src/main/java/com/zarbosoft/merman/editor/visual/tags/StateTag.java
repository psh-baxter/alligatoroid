package com.zarbosoft.merman.editor.visual.tags;

import java.util.Objects;

public class StateTag implements Tag {

  public String value;

  public StateTag() {}

  public StateTag(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(StateTag.class.hashCode(), value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof StateTag && value.equals(((StateTag) obj).value);
  }

  public String toString() {
    return String.format("state:%s", value);
  }
}
