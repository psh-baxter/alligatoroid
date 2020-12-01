package com.zarbosoft.merman.editor;

import java.util.List;

public class InvalidPath extends RuntimeException {
  private final List<String> valid;
  private final List<String> full;

  public InvalidPath(List<String>valid, List<String> full) {
	  this.valid = valid;
	  this.full = full;
	}

  @Override
  public String toString() {
    return String.format("valid %s, full %s", valid, full);
  }
}
