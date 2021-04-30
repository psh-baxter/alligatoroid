package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.nodes.Sequence;

import static com.zarbosoft.rendaw.common.Common.isOrdered;

public abstract class Pattern {
  public static Node characterRange(boolean capture, String low, String high) {
    if (capture)
      return new Sequence()
          .add(new CharacterRangeTerminal(low, high))
          .add(StackStore.pushVarStackSingle);
    else return new CharacterRangeTerminal(low, high);
  }

  public static Node character(boolean capture, String exact) {
    if (capture)
      return new Sequence().add(new CharacterTerminal(exact)).add(StackStore.pushVarStackSingle);
    else return new CharacterTerminal(exact);
  }

  /**
   * Builds nodes to match CharacterEvent and add matched events (var single list) to the stack
   *
   * @return
   * @param capture
   */
  public abstract Node build(boolean capture);

  protected static class CharacterRangeTerminal extends Terminal {
    final String low;
    final String high;

    private CharacterRangeTerminal(String low, String high) {
      this.low = low;
      this.high = high;
    }

    @Override
    protected boolean matches(Event event, Store store) {
      String v = ((CharacterEvent) event).value;
      return isOrdered(low, v) && isOrdered(v, high);
    }
  }

  public static class CharacterTerminal extends Terminal {
    final String exact;

    private CharacterTerminal(String exact) {
      this.exact = exact;
    }

    @Override
    protected boolean matches(Event event, Store store) {
      String v = ((CharacterEvent) event).value;
      return exact.equals(v);
    }
  }

  public static class Matcher {
    private final Grammar grammar;

    public Matcher(Pattern pattern) {
      grammar = new Grammar();
      grammar.add(Grammar.DEFAULT_ROOT_KEY, pattern.build(false));
    }

    public boolean match(Environment env, final String value) {
      try {
        new ParseBuilder<Void>().grammar(grammar).parse(env.splitGlyphs(value));
        return true;
      } catch (final InvalidStream e) {
        return false;
      } catch (NoResults e) {
        return true;
      }
    }
  }
}
