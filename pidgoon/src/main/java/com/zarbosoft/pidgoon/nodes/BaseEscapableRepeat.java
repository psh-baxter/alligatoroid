package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;

/** Match the child 0 or multiple times. */
public abstract class BaseEscapableRepeat<T, K> extends Node<EscapableResult<ROList<K>>> {
  private final Node<EscapableResult<T>> child;
  private long min = 0;
  /** -1 = any number */
  private long max = -1;

  public BaseEscapableRepeat(final Node<EscapableResult<T>> child) {
    super();
    this.child = child;
  }

  public BaseEscapableRepeat<T, K> min(final long i) {
    min = i;
    return this;
  }

  public BaseEscapableRepeat<T, K> max(final long i) {
    max = i;
    return this;
  }

  public abstract void combine(TSList<K> out, T value);

  @Override
  public void context(
      Grammar grammar,
      final Step step,
      final Parent<EscapableResult<ROList<K>>> parent,
      Step.Branch branch,
      ROMap<Object, Reference.RefParent> seen,
      final MismatchCause cause,
      Object color) {
    if (min == 0) parent.advance(grammar, step, branch, new EscapableResult<>(true, ROList.empty), cause);
    if (max == -1 || max > 0)
      child.context(
          grammar,
          step,
          new RepParent<T, K>(this, parent, ROList.empty, color),
          branch,
          seen,
          cause,
          color);
  }

  private static class RepParent<T, K> implements Parent<EscapableResult<T>> {
    public final Parent<EscapableResult<ROList<K>>> parent;
    final ROList<K> collected;
    private final BaseEscapableRepeat<T, K> self;
    private final Object color;

    public RepParent(BaseEscapableRepeat self, Parent<EscapableResult<ROList<K>>> parent, ROList<K> collected, Object color) {
      super();
      this.parent = parent;
      this.self = self;
      this.collected = collected;
      this.color = color;
    }

    @Override
    public void advance(Grammar grammar, Step step, Step.Branch branch, EscapableResult<T> value, MismatchCause mismatchCause) {
      TSList<K> nextCollected = collected.mut();
      self.combine(nextCollected, value.value);
      if (!value.completed || nextCollected.size() >= self.min)
        parent.advance(grammar, step, branch, new EscapableResult<>(value.completed, nextCollected), mismatchCause);
      if (self.max == -1 || nextCollected.size() < self.max)
        self.child.context(
                grammar,
                step,
                new RepParent(self, parent, nextCollected, color),
                branch,
                ROMap.empty,
                mismatchCause,
                color);
    }

    @Override
    public void error(
        Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      parent.error(grammar, step, branch, cause);
    }
  }
}
