package com.zarbosoft.merman.syntax.format;

import java.util.Map;

public class Literal implements Element {
  public String value;

  @Override
  public String format(final Map<String, Object> data) {
    return value;
  }
}
