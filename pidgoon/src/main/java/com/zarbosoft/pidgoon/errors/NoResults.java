package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.parse.Parse;

public class NoResults extends RuntimeException {
  public final Parse state;

  public NoResults(Parse state) {
    this.state = state;
  }
}
