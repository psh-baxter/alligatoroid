package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROMap;

/**
 * A node whose behavior is dynamic.  Implement generate to output the real nodes to use based on store data.
 * @param <T>
 */
public abstract class Dynamic<T extends Store> extends Node {
  @Override
  public void context(
          Parse context,
          Store store,
          Parent parent,
          ROMap<Object, Reference.RefParent> seen,
          Object cause) {
    Pair<T, Node> generated = generate((T) store);
    generated.second.context(context, generated.first, parent, cause);
  }

  /**
   * Called when this dynamic node is reached to generate the actual node to use to parse.
   *
   * @param store
   * @return first: modified store, second: node
   */
  protected abstract Pair<T, Node> generate(T store);
}
