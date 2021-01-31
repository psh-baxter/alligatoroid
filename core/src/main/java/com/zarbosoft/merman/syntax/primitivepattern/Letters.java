package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Union;

public class Letters extends Pattern {
  @Override
  public Node build(I18nEngine i18n) {
    return new Union()
        .add(new CharacterRangeTerminal("a", "z"))
        .add(new CharacterRangeTerminal("A", "Z"));
  }
}
