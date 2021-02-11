package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Position;
import com.zarbosoft.pidgoon.model.Parse;

/** The grammar couldn't match the stream (all branches failed before the stream ended). */
public class InvalidStream extends RuntimeException {
  public final Parse state;
  public final Position position;

  public InvalidStream(final Parse context, final Position position) {
    this.state = context;
    this.position = position;
  }
}
