package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;

public class PatternString extends Pattern {
  public final String string;

  public PatternString(String string) {
    this.string = string;
  }

  @Override
  public Node build() {
    return ParseBuilder.stringSeq(string);
  }
}
