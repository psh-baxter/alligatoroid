package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.events.EscapableResult;
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

public abstract class BaseEscapableSequence<K, T> extends Node<EscapableResult<ROList<T>>> {
  private final TSList<ROPair<Node, Boolean>> children = new TSList<>();
  private Node escape;

  public BaseEscapableSequence<K, T> escape(Node escape) {
    if (this.escape != null) throw new Assertion();
    this.escape = escape;
    return this;
  }

  public BaseEscapableSequence<K, T> add(final Node<ROPair<Boolean, K>> child) {
    children.add(new ROPair<>(child, true));
    return this;
  }

  public BaseEscapableSequence<K, T> addIgnored(final Node<?> child) {
    children.add(new ROPair<>(child, false));
    return this;
  }

  public BaseEscapableSequence<K, T> visit(Consumer<BaseEscapableSequence<K, T>> s) {
    s.accept(this);
    return this;
  }

  @Override
  public void context(
      Grammar grammar,
      final Step step,
      final Parent<EscapableResult<ROList<T>>> parent,
      Step.Branch branch,
      final ROMap<Object, Reference.RefParent> seen,
      final MismatchCause cause,
      Object color) {
    if (children.isEmpty()) {
      parent.advance(grammar, step, branch, new EscapableResult<>(false, ROList.empty), cause);
    } else {
      children
          .get(0)
          .first
          .context(
              grammar,
              step,
              new SeqParent<K, T>(this, parent, 0, ROList.empty, color),
              branch,
              seen,
              cause,
              color);
      if (escape != null)
        escape.context(
            grammar,
            step,
            new Parent<EscapableResult<K>>() {
              @Override
              public void advance(
                  Grammar grammar,
                  Step step,
                  Step.Branch branch,
                  EscapableResult<K> result,
                  MismatchCause mismatchCause) {
                parent.advance(
                    grammar,
                    step,
                    branch,
                    new EscapableResult<>(true, ROList.empty),
                    mismatchCause);
              }

              @Override
              public void error(
                  Grammar grammar, Step step, Step.Branch branch, MismatchCause mismatchCause) {
                parent.error(grammar, step, branch, mismatchCause);
              }
            },
            branch,
            seen,
            cause,
            color);
    }
  }

  public boolean isEmpty() {
    return children.isEmpty();
  }

  protected abstract ROList<T> collect(ROList<T> collection, K result);

  private static class SeqParent<K, T> implements Parent<EscapableResult<K>> {
    final int step;
    final ROList<T> collected;
    private final BaseEscapableSequence<K, T> self;
    private final Parent<EscapableResult<ROList<T>>> parent;
    private final Object color;

    public SeqParent(
        BaseEscapableSequence<K, T> self,
        final Parent<EscapableResult<ROList<T>>> parent,
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
    public void advance(
        Grammar grammar,
        Step step,
        Step.Branch branch,
        EscapableResult<K> result,
        MismatchCause mismatchCause) {
      final int nextStep = this.step + 1;
      ROList<T> newCollected;
      if (self.children.get(this.step).second) newCollected = self.collect(collected, result.value);
      else newCollected = collected;
      if (result.escaped || nextStep >= self.children.size()) {
        parent.advance(
            grammar,
            step,
            branch,
            new EscapableResult<>(result.escaped, newCollected),
            mismatchCause);
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
        if (self.escape != null) {
          self.escape.context(
              grammar,
              step,
              new Parent<EscapableResult<ROList<K>>>() {
                @Override
                public void advance(
                    Grammar grammar,
                    Step step,
                    Step.Branch branch,
                    EscapableResult<ROList<K>> result,
                    MismatchCause mismatchCause) {
                  parent.advance(
                      grammar,
                      step,
                      branch,
                      new EscapableResult<>(true, newCollected),
                      mismatchCause);
                }

                @Override
                public void error(
                    Grammar grammar, Step step, Step.Branch branch, MismatchCause mismatchCause) {
                  parent.error(grammar, step, branch, mismatchCause);
                }
              },
              branch,
              ROMap.empty,
              mismatchCause,
              color);
        }
      }
    }

    @Override
    public void error(Grammar grammar, Step step, Step.Branch branch, MismatchCause mismatchCause) {
      parent.error(grammar, step, branch, mismatchCause);
    }
  }
}
