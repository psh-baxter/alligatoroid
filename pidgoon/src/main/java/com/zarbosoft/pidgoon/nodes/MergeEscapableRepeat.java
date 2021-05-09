package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

/** Match the child 0 or multiple times. */
public class MergeEscapableRepeat<T> extends BaseEscapableRepeat<ROList<T>, T> {
  public MergeEscapableRepeat(Node<EscapableResult<ROList<T>>> child) {
    super(child);
  }

  @Override
  public void combine(TSList<T> out, ROList<T> value) {
    out.addAll(value);
  }
}
