package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.errors.AbortParse;
import com.zarbosoft.pidgoon.model.ExceptionMismatchCause;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

import java.util.ArrayList;
import java.util.List;

/** Recurse via the grammar rule of this name. */
public class Reference<T> extends Node<T> {
  private final Key<T> key;
  private Node<T> base = null;

  public Reference(Key<T> key) {
    super();
    this.key = key;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<T> parent,
          Step.Branch branch,
          final ROMap<Object, RefParent> seen,
          final MismatchCause cause,
          Object color) {
    if (seen.has(key)) {
      seen.get(key).loopParents.add(parent);
      return;
    }
    final RefParent subParent = new RefParent(parent);
    if (base == null) {
      try {
        base = grammar.getNode(key);
      } catch (AbortParse e) {
        parent.error(grammar, step, branch, new ExceptionMismatchCause(this, e));
        return;
      }
    }
    base.context(grammar, step, subParent, branch, seen.mut().put(key, subParent), cause, color);
  }

  /**
   * Needs equals and hashcode
   * @param <T> result type of node stored with this key
   */
  public static class Key<T> {

  }

  public static class RefParent<T> implements Parent<T> {
    public final Parent<T> originalParent;
    public final List<Parent<T>> loopParents = new ArrayList<>();

    public RefParent(final Parent<T> parent) {
      originalParent = parent;
    }

    @Override
    public void advance(
            Grammar grammar, final Step step, Step.Branch branch, T result, final MismatchCause cause) {
      originalParent.advance(grammar, step, branch, result, cause);
      for (final Parent<T> p : loopParents) {
        p.advance(grammar, step, branch, result, cause);
      }
    }

    @Override
    public void error(Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      originalParent.error(grammar, step, branch, cause);
    }
  }
}
