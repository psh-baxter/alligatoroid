package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.Pidgoon;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.model.Parse;
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
        Pidgoon.prepare(grammar, root, store, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
  }

  public ParseEventSink(final Parse step, final Grammar grammar) {
    this.context = step;
    this.grammar = grammar;
  }

  @Override
  public ParseEventSink<O> push(final Event event, final Object at) {
    if (ended()) throw new Assertion();
    final Parse nextStep = Pidgoon.step(context, new Position(event, at));
    return new ParseEventSink<O>(nextStep, grammar);
  }

  public boolean ended() {
    return context.branches.isEmpty();
  }

  public O result() {
    if (context.completed.isEmpty()) throw new NoResults(context);
    return (O) context.completed.get(0).result();
  }

  public Parse context() {
    return context;
  }
}
