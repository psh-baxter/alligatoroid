package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Step;

/** There were too many branches when parsing. This is configurable in the Parse. */
public class GrammarTooUncertain extends RuntimeException {
  public final Step step;

  public GrammarTooUncertain(final Step step) {
    this.step = step;
  }
}
