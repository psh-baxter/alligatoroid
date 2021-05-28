package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Leaf;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROMap;

public class Pidgoon {
  public static <R, S extends Step<R>> S prepare(
      final Grammar grammar, final Reference.Key<R> root, S step) {
    grammar
        .getNode(root)
        .context(
            grammar,
            step,
            new Parent<R>() {
              @Override
              public void advance(
                  Grammar grammar,
                  final Step step,
                  Leaf leaf,
                  R result,
                  final MismatchCause cause) {
                step.completed.add(result);
              }

              @Override
              public void error(
                  Grammar grammar, final Step step, Leaf leaf, final MismatchCause cause) {
                step.errors.add(cause);
              }
            },
            null,
            ROMap.empty,
            null,
            null);
    return step;
  }

  /**
   * Advance the parse through the next position
   *
   * @param step
   * @param position the next event to parse
   * @return parse after consuming current position, or null if EOF reached
   */
  public static <E, R> Step<R> parallelStep(
      Grammar grammar, int uncertaintyLimit, Step<R> step, E event) {
    if (step.leaves.isEmpty()) return null;

    final Step<R> nextStep = new Step<R>();

    for (final Leaf<E> leaf : step.leaves) leaf.parse(grammar, nextStep, event);

    if (nextStep.leaves.size() > uncertaintyLimit) throw new GrammarTooUncertain(nextStep);
    if (nextStep.leaves.isEmpty() && nextStep.errors.size() == step.leaves.size())
      throw new InvalidStream(nextStep);

    return nextStep;
  }
}
