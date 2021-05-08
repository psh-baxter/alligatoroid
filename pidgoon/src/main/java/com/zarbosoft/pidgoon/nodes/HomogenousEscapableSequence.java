package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.rendaw.common.ROList;

public class HomogenousEscapableSequence<T> extends BaseEscapableSequence<T, T> {
  @Override
  protected ROList<T> collect(ROList<T> collection, T result) {
    return collection.mut().add(result);
  }
}
