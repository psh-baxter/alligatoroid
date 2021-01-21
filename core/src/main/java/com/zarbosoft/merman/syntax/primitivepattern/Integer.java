package com.zarbosoft.merman.syntax.primitivepattern;

import com.google.common.collect.Range;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.nodes.Terminal;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

public class Integer extends Pattern {
  @Override
  public Node build() {
    return new Sequence()
        .add(new Repeat(new Terminal((byte) '-')).max(1))
        .add(new Repeat(new Terminal(Range.closed((byte) '0', (byte) '9'))).min(1));
  }
}
