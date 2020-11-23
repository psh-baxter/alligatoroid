package com.zarbosoft.merman.syntax.format;

import java.util.Map;

public class Reference implements Element {

  public String name;

  @Override
  public String format(final Map<String, Object> data) {
    return data.getOrDefault(name, "BADKEY").toString();
  }
}
