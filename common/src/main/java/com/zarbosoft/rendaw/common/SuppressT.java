package com.zarbosoft.rendaw.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SuppressT<T> {
  private final List<Pair<Class, Supplier<T>>> handlers = new ArrayList<>();

  public SuppressT<T> handle(Class match, Supplier<T> supplier) {
    handlers.add(new Pair<>(match, supplier));
    return this;
  }

  public T go(Common.UncheckedSupplier<T> call, Supplier<T> orElse) {
    try {
      return call.get();
    } catch (Exception e) {
      for (Pair<Class, Supplier<T>> handler : handlers) {
        if (handler.first.isAssignableFrom(e.getClass())) {
          return handler.second.get();
        }
      }
      return orElse.get();
    }
  }
}
