package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class MergeSequence<T> extends BaseSequence<ROList<T>, T> {
  @Override
  protected ROList<T> collect(ROList<T> collection, ROList<T> result) {
    if (result.none()) return collection;
    return collection.mut().addAll(result);
  }
}
