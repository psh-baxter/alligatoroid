package com.zarbosoft.merman.syntax.primitivepattern;

import com.google.common.collect.Range;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.nodes.Terminal;

public class Digits extends Pattern {
  @Override
  public Node build() {
    return new Terminal(Range.closed((byte) '0', (byte) '9'));
  }
}
