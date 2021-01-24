package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.rendaw.common.ROMap;

public class Reference implements Element {

  public final String name;

  public Reference(String name) {
    this.name = name;
  }

  @Override
  public String format(final ROMap<String, Object> data) {
    return data.getOr(name, () -> String.format("<BADKEY:%s>", name)).toString();
  }
}
