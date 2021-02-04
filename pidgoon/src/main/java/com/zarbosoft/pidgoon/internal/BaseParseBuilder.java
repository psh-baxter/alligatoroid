package com.zarbosoft.pidgoon.internal;

import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Store;

public abstract class BaseParseBuilder<P extends BaseParseBuilder<P>> {
  protected Grammar grammar;
  protected Object root = Grammar.DEFAULT_ROOT_KEY;
  protected Store initialStore;
  protected int errorHistoryLimit;
  protected int uncertaintyLimit;
  protected boolean dumpAmbiguity;

  public BaseParseBuilder() {
    super();
    errorHistoryLimit = 1;
    uncertaintyLimit = 1000;
  }

  public BaseParseBuilder(final BaseParseBuilder<P> other) {
    grammar = other.grammar;
    root = other.root;
    initialStore = other.initialStore;
    errorHistoryLimit = other.errorHistoryLimit;
    uncertaintyLimit = other.uncertaintyLimit;
    dumpAmbiguity = other.dumpAmbiguity;
  }

  public P grammar(final Grammar grammar) {
    if (this.grammar != null) throw new IllegalArgumentException("Grammar already specified");
    if (grammar == null) throw new IllegalArgumentException("Argument is null.");
    final P out = split();
    out.grammar = grammar;
    return out;
  }

  protected abstract P split();

  public P root(final Object key) {
    if (!this.root.equals(Grammar.DEFAULT_ROOT_KEY))
      throw new IllegalArgumentException("Node already specified");
    final P out = split();
    out.root = key;
    return out;
  }

  public P store(final Store store) {
    if (this.initialStore != null)
      throw new IllegalArgumentException("Initial store supplier already specified");
    final P out = split();
    out.initialStore = store;
    return out;
  }

  public P errorHistory(final int limit) {
    if (this.errorHistoryLimit != 1)
      throw new IllegalArgumentException("Error history limit already specified");
    final P out = split();
    out.errorHistoryLimit = limit;
    return out;
  }

  public P uncertainty(final int limit) {
    if (this.uncertaintyLimit != 1000)
      throw new IllegalArgumentException("Uncertainty limit already specified");
    final P out = split();
    out.uncertaintyLimit = limit;
    return out;
  }

  public P dumpAmbiguity(final boolean dumpAmbiguity) {
    if (this.dumpAmbiguity) throw new IllegalArgumentException("Dump ambiguity already specified");
    final P out = split();
    out.dumpAmbiguity = dumpAmbiguity;
    return out;
  }
}
