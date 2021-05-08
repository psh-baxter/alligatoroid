package com.zarbosoft.pidgoon.model;

import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class Node<T> {
  public abstract void context(
          Grammar grammar, Step step, Parent<T> parent, Step.Branch branch, ROMap<Object, Reference.RefParent> seen, MismatchCause cause, Object color);
}
