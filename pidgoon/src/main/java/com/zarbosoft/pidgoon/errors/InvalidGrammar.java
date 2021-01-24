package com.zarbosoft.pidgoon.errors;

/** An error in the grammar definition. */
public class InvalidGrammar extends Error {
  public InvalidGrammar(final String text) {
    super(text);
  }
}
