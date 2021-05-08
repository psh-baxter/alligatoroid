package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class HomogenousSequence<T> extends BaseSequence<T, T> {
  @Override
  protected ROList<T> collect(ROList<T> collection, T result) {
    return collection.mut().add(result);
  }
}
