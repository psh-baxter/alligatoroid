package com.zarbosoft.pidgoon.model;

import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Format;

import java.util.HashMap;
import java.util.Map;

public class Grammar {
  protected final Map<Object, Node> nodes;

  public Grammar() {
    this.nodes = new HashMap<>();
  }

  public Grammar(Grammar other) {
    this.nodes = new HashMap<>(other.nodes);
  }

  public <T> Grammar add(final Reference.Key<T> key, final Node<T> node) {
    if (nodes.containsKey(key))
      throw new AssertionError(Format.format("Node with name [%s] already exists.", key));
    nodes.put(key, node);
    return this;
  }

  public <T> Node<T> getNode(final Reference.Key<T> key) {
    if (!nodes.containsKey(key)) throw new RuntimeException(Format.format("No rule named %s", key));
    return nodes.get(key);
  }
}
