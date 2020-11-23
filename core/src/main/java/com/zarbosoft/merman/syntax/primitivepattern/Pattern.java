package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.errors.InvalidStream;

public abstract class Pattern {
  public static Pattern repeatedAny;

  static {
    repeatedAny = new Repeat0();
    ((Repeat0) repeatedAny).pattern = new Any();
  }

  public abstract Node build();

  public class Matcher {
    private final Grammar grammar;

    public Matcher() {
      grammar = new Grammar();
      grammar.add("root", build());
    }

    public boolean match(final String value) {
      try {
        new ParseBuilder<Void>().grammar(grammar).parse(value);
        return true;
      } catch (final InvalidStream e) {
        return false;
      }
    }
  }
}
