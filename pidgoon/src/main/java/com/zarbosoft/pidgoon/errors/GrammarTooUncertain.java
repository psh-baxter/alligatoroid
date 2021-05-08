package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Step;

/** There were too many branches when parsing. This is configurable in the Parse. */
public class GrammarTooUncertain extends RuntimeException {
  public final Step context;

  public GrammarTooUncertain(final Step context) {
    this.context = context;
  }
}
