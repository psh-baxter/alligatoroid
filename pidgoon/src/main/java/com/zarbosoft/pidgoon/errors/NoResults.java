package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Step;

public class NoResults extends RuntimeException {
  public final Step state;

  public NoResults(Step state) {
    this.state = state;
  }
}
