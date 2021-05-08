package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.nodes.Reference;

public abstract class BaseParseBuilder<O, P extends BaseParseBuilder<O, P>> {
  protected final Reference.Key<O> root;
  protected Grammar grammar;
  protected int uncertaintyLimit = 1000;

  public BaseParseBuilder(Reference.Key<O> root) {
    super();
    this.root = root;
  }

  public BaseParseBuilder(final BaseParseBuilder<O, P> other) {
    grammar = other.grammar;
    root = other.root;
    uncertaintyLimit = other.uncertaintyLimit;
  }

  public P grammar(final Grammar grammar) {
    if (this.grammar != null) throw new IllegalArgumentException("Grammar already specified");
    if (grammar == null) throw new IllegalArgumentException("Argument is null.");
    final P out = split();
    out.grammar = grammar;
    return out;
  }

  protected abstract P split();

  public P uncertainty(final int limit) {
    if (this.uncertaintyLimit != 1000)
      throw new IllegalArgumentException("Uncertainty limit already specified");
    final P out = split();
    out.uncertaintyLimit = limit;
    return out;
  }
}
