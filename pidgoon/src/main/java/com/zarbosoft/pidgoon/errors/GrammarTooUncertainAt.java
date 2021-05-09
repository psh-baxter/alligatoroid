package com.zarbosoft.pidgoon.errors;

/** There were too many branches when parsing. This is configurable in the Parse. */
public class GrammarTooUncertainAt extends RuntimeException {
  public final Object at;
  public final GrammarTooUncertain e;

  public GrammarTooUncertainAt(Object at, GrammarTooUncertain e) {
    this.at = at;
    this.e = e;
  }
}
