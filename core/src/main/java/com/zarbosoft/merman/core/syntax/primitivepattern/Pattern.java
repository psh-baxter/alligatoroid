package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROList;

public abstract class Pattern {
  /**
   * Builds nodes to match CharacterEvent and add matched events (var single list) to the stack
   *
   * @return
   * @param capture
   */
  public abstract Node<ROList<String>> build(boolean capture);

  public final static Reference.Key<ROList<String>> ROOT_KEY = new Reference.Key<>();
  public static class Matcher {
    private final Grammar grammar;

    public Matcher(Pattern pattern) {
      grammar = new Grammar();
      grammar.add(ROOT_KEY, pattern.build(false));
    }

    public boolean match(Environment env, final String value) {
      try {
        new ParseBuilder<>(ROOT_KEY).grammar(grammar).parse(env.splitGlyphEvents(value));
        return true;
      } catch (final InvalidStream e) {
        return false;
      } catch (NoResults e) {
        return true;
      }
    }
  }
}
