package com.zarbosoft.pidgoon.model;

public class ExceptionMismatchCause extends MismatchCause {
  public final Exception error;

  public ExceptionMismatchCause(Node node, Object color, Exception error) {
    super(node, color);
    this.error = error;
  }

  @Override
  public String toString() {
    return super.toString() + ": " + error.toString();
  }
}
