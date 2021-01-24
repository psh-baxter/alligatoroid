package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.InvalidGrammar;

import java.util.HashMap;
import java.util.Map;

public class Grammar {
  public static final Object DEFAULT_ROOT_KEY = new Object();
  protected final Map<Object, Node> nodes;

  public Grammar() {
    this.nodes = new HashMap<>();
  }

  public Grammar(Grammar other) {
    this.nodes = new HashMap<>(other.nodes);
  }

  public Grammar add(final Object key, final Node node) {
    if (nodes.containsKey(key))
      throw new AssertionError(String.format("Node with name [%s] already exists.", key));
    nodes.put(key, node);
    return this;
  }

  public Node getNode(final Object key) {
    if (!nodes.containsKey(key)) throw new InvalidGrammar(String.format("No rule named %s", key));
    return nodes.get(key);
  }
}
