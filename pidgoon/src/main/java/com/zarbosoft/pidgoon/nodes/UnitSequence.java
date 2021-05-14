package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

/**
 * Parses a sequence, but only produces a single value
 *
 * @param <T>
 */
public class UnitSequence<T> extends Node<T> {
  boolean found = false;
  TSList<ROPair<Node, Boolean>> children = new TSList<>();

  public UnitSequence<T> add(final Node<T> child) {
    if (found) throw new Assertion("Multiple non-ignored children!");
    found = true;
    children.add(new ROPair<>(child, true));
    return this;
  }

  public UnitSequence<T> addIgnored(final Node child) {
    children.add(new ROPair<>(child, false));
    return this;
  }

  public UnitSequence<T> visit(Consumer<UnitSequence<T>> s) {
    s.accept(this);
    return this;
  }

  @Override
  public void context(
      Grammar grammar,
      final Step step,
      final Parent<T> parent,
      Step.Branch branch,
      final ROMap<Object, Reference.RefParent> seen,
      final MismatchCause cause,
      Object color) {
    if (children.isEmpty()) {
      throw new Assertion("No children added!");
    } else {
      children
          .get(0)
          .first
          .context(
              grammar,
              step,
              new SeqParent<T>(this, parent, 0, null, color),
              branch,
              seen,
              cause,
              color);
    }
  }

  public boolean isEmpty() {
    return children.isEmpty();
  }

  private static class SeqParent<T> implements Parent<Object> {
    final int step;
    final T collected;
    private final UnitSequence<T> self;
    private final Parent<T> parent;
    private final Object color;

    public SeqParent(
        UnitSequence<T> self, final Parent<T> parent, final int step, T collected, Object color) {
      this.parent = parent;
      this.step = step;
      this.self = self;
      this.collected = collected;
      this.color = color;
    }

    @Override
    public void advance(
        Grammar grammar,
        Step step,
        Step.Branch branch,
        Object result,
        MismatchCause mismatchCause) {
      final int nextStep = this.step + 1;
      T newCollected;
      if (self.children.get(this.step).second) newCollected = (T) result;
      else newCollected = collected;
      if (nextStep >= self.children.size()) {
        parent.advance(grammar, step, branch, newCollected, mismatchCause);
      } else {
        self.children
            .get(nextStep)
            .first
            .context(
                grammar,
                step,
                new SeqParent<T>(self, parent, nextStep, newCollected, color),
                branch,
                ROMap.empty,
                mismatchCause,
                color);
      }
    }

    @Override
    public void error(Grammar grammar, Step step, Step.Branch branch, MismatchCause mismatchCause) {
      parent.error(grammar, step, branch, mismatchCause);
    }
  }
}
