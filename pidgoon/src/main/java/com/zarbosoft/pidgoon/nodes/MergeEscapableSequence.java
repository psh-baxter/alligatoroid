package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.rendaw.common.ROList;

public class MergeEscapableSequence<T> extends BaseEscapableSequence<ROList<T>, T> {
  @Override
  protected ROList<T> collect(ROList<T> collection, ROList<T> result) {
    if (result.none()) return collection;
    return collection.mut().addAll(result);
  }
}
