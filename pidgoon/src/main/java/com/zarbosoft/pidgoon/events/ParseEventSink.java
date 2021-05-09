package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.Pidgoon;
import com.zarbosoft.pidgoon.errors.AbortParse;
import com.zarbosoft.pidgoon.errors.AbortParseAt;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertainAt;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.InvalidStreamAt;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Assertion;

/**
 * Created by Parse. A push-based parse (user pushes events when they are available). This is
 * immutable - every `push` creates a new EventStream. This allows branching if you nest parses.
 *
 * @param <O> Parse result type. Returned by `finish`.
 */
public class ParseEventSink<O> implements EventSink<ParseEventSink<O>> {
  private final Step<O> step;
  private final Grammar grammar;
  private final int uncertaintyLimit;

  public ParseEventSink(
      final Grammar grammar, final Reference.Key<O> root, final int uncertaintyLimit) {
    this.grammar = grammar;
    this.uncertaintyLimit = uncertaintyLimit;
    this.step = Pidgoon.prepare(grammar, root);
  }

  public ParseEventSink(final Step<O> step, final Grammar grammar, int uncertaintyLimit) {
    this.step = step;
    this.grammar = grammar;
    this.uncertaintyLimit = uncertaintyLimit;
  }

  @Override
  public ParseEventSink<O> push(final Event event, final Object at) {
    if (ended()) throw new Assertion();
    final Step<O> nextStep;
    try {
      nextStep = Pidgoon.step(grammar, uncertaintyLimit, step, event);
    } catch (GrammarTooUncertain e) {
      throw new GrammarTooUncertainAt(at, e);
    } catch (InvalidStream e) {
      throw new InvalidStreamAt(at, e);
    } catch (AbortParse e) {
      throw new AbortParseAt(at, e);
    }
    return new ParseEventSink<O>(nextStep, grammar, uncertaintyLimit);
  }

  public boolean ended() {
    return step.branches.isEmpty();
  }

  public O result() {
    if (step.completed.isEmpty()) throw new NoResults(step);
    return step.completed.get(0);
  }

  public Step<O> context() {
    return step;
  }
}
