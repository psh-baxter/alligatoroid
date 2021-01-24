package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.NoResultsError;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Assertion;

import java.util.List;

/**
 * Created by Parse. A push-based parse (user pushes events when they are available). This is
 * immutable - every `push` creates a new EventStream. This allows branching if you nest parses.
 *
 * @param <O> Parse result type. Returned by `finish`.
 */
public class ParseEventSink<O> implements EventSink {
  private final Parse context;
  private final Grammar grammar;

  public ParseEventSink(
      final Grammar grammar,
      final Object root,
      final Store store,
      final int errorHistoryLimit,
      final int uncertaintyLimit,
      final boolean dumpAmbiguity) {
    this.grammar = grammar;
    this.context =
        Parse.prepare(
            grammar, root, store, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
  }

  public ParseEventSink(final Parse step, final Grammar grammar) {
    this.context = step;
    this.grammar = grammar;
  }

  @Override
  public ParseEventSink<O> push(final Event event, final Object at) {
    if (ended()) throw new Assertion();
    final Parse nextStep = context.step(new Position(event, at));
    return new ParseEventSink<O>(nextStep, grammar);
  }

  public boolean ended() {
    return context.leaves.isEmpty();
  }

  public boolean hasResult() {
    return !context.results.isEmpty();
  }

  public O result() {
    return allResults().get(0);
  }

  public List<O> allResults() {
    if (context.results.isEmpty()) throw new NoResultsError(context);
    return (List<O>) context.results;
  }

  public Parse context() {
    return context;
  }
}
