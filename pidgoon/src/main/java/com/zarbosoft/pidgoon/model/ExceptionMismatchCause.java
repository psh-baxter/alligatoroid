package com.zarbosoft.pidgoon.model;

public class ExceptionMismatchCause extends MismatchCause {
  public final Exception error;

  public ExceptionMismatchCause(Node node, Exception error) {
    super(node);
    this.error = error;
  }

  @Override
  public String toString() {
    return super.toString() + ": " + error.toString();
  }
}
