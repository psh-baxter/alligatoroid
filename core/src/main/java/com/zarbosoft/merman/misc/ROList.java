package com.zarbosoft.merman.misc;

import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface ROList<T> extends Iterable<T> {
  @SuppressWarnings("rawtypes")
  public static final ROList empty = new TSList();

  public boolean isEmpty();

  public int size();

  public T get(int i);

    TSSet<T> toSet();

    ROList<T> sublist(int start, int end);
}
