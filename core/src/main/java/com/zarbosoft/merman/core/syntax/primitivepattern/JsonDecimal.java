package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;

public class JsonDecimal extends Pattern {
  @Override
  public Node build(boolean capture) {
    Node digits = Pattern.characterRange(capture,"0", "9");
    return new Sequence()
        .add(new Repeat(Pattern.characterRange(capture, "-", "-")).max(1))
        .add(
            new Union()
                .add(Pattern.characterRange(capture, "0", "0"))
                .add(Pattern.characterRange(capture, "1", "9"))
                .add(new Repeat(digits)))
        .add(
            new Repeat(
                    new Sequence()
                        .add(Pattern.characterRange(capture, ".", "-"))
                        .add(new Repeat(digits)))
                .max(1))
        .add(
            new Repeat(
                    new Sequence()
                        .add(new Repeat(Pattern.characterRange(capture, "e", "E")).max(1))
                        .add(new Repeat(Pattern.characterRange(capture, "+", "-")).max(1))
                        .add(new Repeat(digits).min(1)))
                .max(1));
  }
}
