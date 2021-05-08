package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

/** Match each child exactly once. */
public class MergeSet<T> extends BaseSet<ROList<T>, T> {
  @Override
  public TSList<T> combine(TSList<T> out, ROList<T> value) {
    return out.addAll(value);
  }
}
