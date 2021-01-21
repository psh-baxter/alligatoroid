package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.misc.TSList;
import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.merman.syntax.style.Style;

public class StyleBuilder {
  private final TSSet<String> with = new TSSet<>();
  private final TSSet<String> without = new TSSet<>();
  private final Style.Spec style = new Style.Spec();

  public StyleBuilder tag(final String tag) {
    with.add(tag);
    return this;
  }

  public StyleBuilder split(final boolean on) {
    style.split = on;
    return this;
  }

  public StyleBuilder spaceBefore(final int x) {
    style.spaceBefore = x;
    return this;
  }

  public StyleBuilder spaceAfter(final int x) {
    style.spaceAfter = x;
    return this;
  }

  public StyleBuilder spaceTransverseBefore(final int x) {
    style.spaceTransverseBefore = x;
    return this;
  }

  public StyleBuilder spaceTransverseAfter(final int x) {
    style.spaceTransverseAfter = x;
    return this;
  }

  public Style.Spec build() {
    style.with = with.ro();
    style.without = without.ro();
    return style;
  }

  public StyleBuilder alignment(final String name) {
    style.alignment = name;
    return this;
  }

  public StyleBuilder notag(final String tag) {
    without.add(tag);
    return this;
  }
}
