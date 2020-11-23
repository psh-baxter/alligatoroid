package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;

public class PatternString extends Pattern {
  public String string;

  @Override
  public Node build() {
    return ParseBuilder.stringSeq(string);
  }
}
