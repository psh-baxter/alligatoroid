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
  public Node build(I18nEngine i18n) {
    return new Repeat(pattern.build(i18n)).min(1);
  }
}
