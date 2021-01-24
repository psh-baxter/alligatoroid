package com.zarbosoft.pidgoon.errors;

/** Raise in an operator to cause a branch to fail. Other branches will continue to parse. */
public class AbortParse extends RuntimeException {
  public AbortParse(final String s) {
    super(s);
  }

  public AbortParse(final String s, Throwable source) {
    super(s, source);
  }

  public AbortParse() {}

  public AbortParse(final Throwable source) {
    super(source);
  }
}
