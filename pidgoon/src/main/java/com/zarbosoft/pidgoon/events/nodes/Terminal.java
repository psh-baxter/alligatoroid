package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;

/** Base node to match a single event. Define `matches` to use. */
public abstract class Terminal<E, V> extends Node<V> {
  public Terminal() {}

  @Override
  public void context(
      Grammar grammar,
      Step step,
      Parent<V> parent,
      Step.Branch branch,
      ROMap<Object, Reference.RefParent> seen,
      MismatchCause cause,
      Object color) {
    step.branches.add(new TerminalBranch<E, V>(this, parent, color));
  }

  /**
   * Defines conditions and checks if the current terminal matches those conditions.
   *
   * @param event current terminal
   * @param store Current store - creation in progress so may be modified
   * @return true if conditions match
   */
  protected abstract ROPair<Boolean, V> matches(E event);

  public static class TerminalBranch<E, V> extends Step.Branch<E> {
    public Terminal<E, V> self;
    private Parent<V> parent;
    private Object color;

    public TerminalBranch(Terminal<E, V> self, Parent<V> parent, Object color) {
      this.self = self;
      this.parent = parent;
      this.color = color;
    }

    @Override
    public <T> T color() {
      return (T) color;
    }

    @Override
    public void parse(Grammar grammar, Step step, E event) {
      ROPair<Boolean, V> res = self.matches(event);
      if (res.first) {
        parent.advance(grammar, step, this, res.second, new MismatchCause(self));
      } else {
        parent.error(grammar, step, this, new MismatchCause(self));
      }
    }
  }
}
