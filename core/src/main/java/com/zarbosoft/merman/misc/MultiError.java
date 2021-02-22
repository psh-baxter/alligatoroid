package com.zarbosoft.merman.misc;

import java.util.ArrayList;
import java.util.List;

public class MultiError {
  public final List errors = new ArrayList();

  public MultiError add(Object e) {
    errors.add(e);
    return this;
  }

  public void raise() {
    if (errors.isEmpty()) return;
    throw new Exception(errors);
  }

  public boolean isEmpty() {
    return errors.isEmpty();
  }

  public static class Exception extends RuntimeException {
    public final List errors;

    public Exception(List errors) {
      this.errors = errors;
    }

    @Override
    public String toString() {
      StringBuilder out = new StringBuilder();
      for (Object error : errors) {
        out.append(error.toString());
        out.append("\n");
      }
      return out.toString();
    }
  }
}
