package com.zarbosoft.luxem.read;

public class InvalidStream extends RuntimeException {

  private final int offset;

  public InvalidStream(final int offset, final String string) {
    super(string);
    this.offset = offset;
  }

  @Override
  public String getMessage() {
    return String.format("[offset %s] %s", offset, super.getMessage());
  }
}
