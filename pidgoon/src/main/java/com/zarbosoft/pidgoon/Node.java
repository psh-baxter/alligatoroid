package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

public abstract class Node {
  public void context(
      final Parse context, final Store store, final Parent parent, final Object cause) {
    context(context, store, parent, HashTreePMap.empty(), cause);
  }

  public abstract void context(
      Parse context, Store store, Parent parent, PMap<Object, RefParent> seen, Object cause);
}
