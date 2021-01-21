package com.zarbosoft.merman.syntax.primitivepattern;

import com.google.common.collect.Range;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.nodes.Terminal;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;

public class JsonDecimal extends Pattern {
  @Override
  public Node build() {
    Terminal digits = new Terminal(Range.closed((byte) '0', (byte) '9'));
    return new Sequence()
        .add(new Repeat(new Terminal((byte) '-')).max(1))
        .add(
            new Union()
                .add(new Terminal((byte) '0'))
                .add(new Terminal(Range.closed((byte) '1', (byte) '9')))
                .add(new Repeat(digits)))
        .add(
            new Repeat(new Sequence().add(new Terminal((byte) '.')).add(new Repeat(digits))).max(1))
        .add(
            new Repeat(
                    new Sequence()
                        .add(new Repeat(new Terminal((byte) 'e', (byte) 'E')).max(1))
                        .add(new Repeat(new Terminal((byte) '+', (byte) '-')).max(1))
                        .add(new Repeat(digits).min(1)))
                .max(1));
  }
}
