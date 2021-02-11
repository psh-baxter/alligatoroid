package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.model.Parse;

/** There were too many branches when parsing. This is configurable in the Parse. */
public class GrammarTooUncertain extends RuntimeException {
  public final Object position;
  public final Parse context;

  public GrammarTooUncertain(final Parse context, final Object position) {
    this.context = context;
    this.position = position;
  }
}
