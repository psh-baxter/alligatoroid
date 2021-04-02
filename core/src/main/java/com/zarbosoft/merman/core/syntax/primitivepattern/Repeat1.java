package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Repeat1 extends Pattern {
  public final Pattern pattern;

  public Repeat1(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node build() {
    return new Repeat(pattern.build()).min(1);
  }
}
