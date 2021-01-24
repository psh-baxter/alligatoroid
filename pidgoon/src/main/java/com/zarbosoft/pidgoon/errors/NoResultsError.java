package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.parse.Parse;

public class NoResultsError extends RuntimeException {
  public final Parse state;

  public NoResultsError(Parse state) {
    this.state = state;
  }
}
