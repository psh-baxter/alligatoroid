package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

/** Matches any event/byte. */
public class Wildcard extends Node<Object> {
  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<Object> parent,
          Step.Branch branch, final ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    step.branches.add(
        new Step.Branch<Object>() {
          @Override
          public <T> T color() {
            return (T) color;
          }

          @Override
          public void parse(Grammar grammar, final Step step, Object object) {
            parent.advance(grammar, step, this, object, new MismatchCause(Wildcard.this));
          }
        });
  }
}
