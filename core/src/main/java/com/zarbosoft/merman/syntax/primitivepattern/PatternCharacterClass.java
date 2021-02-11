package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;

public class PatternCharacterClass extends Pattern {
  private final ROList<ROPair<String, String>> ranges;

  public PatternCharacterClass(ROList<ROPair<String, String>> ranges) {
    this.ranges = ranges;
  }

  @Override
  public Node build(I18nEngine i18n) {
    Union union = new Union();
    for (ROPair<String, String> range : ranges) {
      union.add(new CharacterRangeTerminal(range.first, range.second));
    }
    return union;
  }
}
