package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Step;

/** The grammar couldn't match the stream (all branches failed before the stream ended). */
public class InvalidStream extends RuntimeException {
  public final Step step;

  public InvalidStream(final Step step) {
    this.step = step;
  }
}
