package com.zarbosoft.merman.misc;

import java.util.ArrayList;
import java.util.List;

public class MultiError extends RuntimeException {
  public final List errors = new ArrayList();

  public void add(Object e) {
    errors.add(e);
  }

  public void raise() {
    if (errors.isEmpty()) return;
    throw this;
  }

  public boolean isEmpty() {
    return errors.isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < errors.size(); ++i) {
      String t = errors.get(i).toString();

    }
    for (Object error : errors) {
      out.append(error.toString());
      out.append("\n");
    }
    return out.toString();
  }
}
