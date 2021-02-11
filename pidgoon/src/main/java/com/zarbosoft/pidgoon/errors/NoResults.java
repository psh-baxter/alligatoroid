package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Parse;

public class NoResults extends RuntimeException {
  public final Parse state;

  public NoResults(Parse state) {
    this.state = state;
  }
}
