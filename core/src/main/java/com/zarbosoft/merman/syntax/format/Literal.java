package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.merman.misc.ROMap;

public class Literal implements Element {
  public final String value;

  public Literal(String value) {
    this.value = value;
  }

  @Override
  public String format(final ROMap<String, Object> data) {
    return value;
  }
}
