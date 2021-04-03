package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;

public class JsonDecimal extends Pattern {
  @Override
  public Node build() {
    CharacterRangeTerminal digits = new CharacterRangeTerminal("0", "9");
    return new Sequence()
        .add(new Repeat(new CharacterRangeTerminal("-", "-")).max(1))
        .add(
            new Union()
                .add(new CharacterRangeTerminal("0", "0"))
                .add(new CharacterRangeTerminal("1", "9"))
                .add(new Repeat(digits)))
        .add(
            new Repeat(
                    new Sequence()
                        .add(new CharacterRangeTerminal(".", "-"))
                        .add(new Repeat(digits)))
                .max(1))
        .add(
            new Repeat(
                    new Sequence()
                        .add(new Repeat(new CharacterRangeTerminal("e", "E")).max(1))
                        .add(new Repeat(new CharacterRangeTerminal("+", "-")).max(1))
                        .add(new Repeat(digits).min(1)))
                .max(1));
  }
}
