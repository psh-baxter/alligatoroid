package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

/** Match the child 0 or multiple times. */
public class MergeRepeat<T> extends BaseRepeat<ROList<T>, T> {
  public MergeRepeat(Node<ROList<T>> child) {
    super(child);
  }

  @Override
  public void combine(TSList<T> out, ROList<T> value) {
    out.addAll(value);
  }
}
