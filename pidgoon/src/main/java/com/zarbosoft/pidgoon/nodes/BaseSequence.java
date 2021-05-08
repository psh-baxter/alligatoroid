package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public abstract class BaseSequence<K, T> extends Node<ROList<T>> {
  private final TSList<ROPair<Node, Boolean>> children = new TSList<>();

  public BaseSequence<K, T> add(final Node<K> child) {
    children.add(new ROPair<>(child, true));
    return this;
  }

  public BaseSequence<K, T> addIgnored(final Node<?> child) {
    children.add(new ROPair<>(child, false));
    return this;
  }

  public BaseSequence<K, T> visit(Consumer<BaseSequence<K, T>> s) {
    s.accept(this);
    return this;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<ROList<T>> parent,
          Step.Branch branch,
          final ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    if (children.isEmpty()) {
      parent.advance(grammar, step, branch, ROList.empty, cause);
    } else {
      children
          .get(0)
          .first
          .context(
                  grammar, step,
              new SeqParent<K, T>(this, parent, 0, ROList.empty, color),
              branch,
              seen,
              cause,
              color);
    }
  }

  public boolean isEmpty() {
    return children.isEmpty();
  }

  private static class SeqParent<K, T> implements Parent<K> {
    final int step;
    final ROList<T> collected;
    private final BaseSequence<K, T> self;
    private final Parent<ROList<T>> parent;
    private final Object color;

    public SeqParent(
        BaseSequence<K, T> self,
        final Parent<ROList<T>> parent,
        final int step,
        ROList<T> collected,
        Object color) {
      this.parent = parent;
      this.step = step;
      this.self = self;
      this.collected = collected;
      this.color = color;
    }

    @Override
    public void advance(Grammar grammar, Step step, Step.Branch branch, K result, MismatchCause mismatchCause) {
      final int nextStep = this.step + 1;
      ROList<T> newCollected;
      if (self.children.get(this.step).second) newCollected = self.collect(collected,result);
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
                new SeqParent<K, T>(self, parent, nextStep, collected, color),
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

  protected abstract ROList<T> collect(ROList<T> collection, K result);
}
