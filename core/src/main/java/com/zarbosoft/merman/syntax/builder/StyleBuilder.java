package com.zarbosoft.merman.syntax.builder;

import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.TSSet;

public class StyleBuilder {
  private final Style.Spec spec = new Style.Spec();
  private final TSSet<String> with = new TSSet<>();
  private final TSSet<String> without = new TSSet<>();

  public Style.Spec build() {
    spec.with = with.ro();
    spec.without = without.ro();
    return spec;
  }

  public StyleBuilder with(String tag) {
    with.add(tag);
    return this;
  }

  public StyleBuilder without(String tag) {
    without.add(tag);
    return this;
  }

  public StyleBuilder space(int px) {
    spec.space = px;
    return this;
  }

  public StyleBuilder align(String name) {
    spec.alignment = name;
    return this;
  }

  public StyleBuilder split() {
    spec.split = true;
    return this;
  }
}
