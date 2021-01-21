package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.misc.ROList;

import java.util.List;

public class InvalidPath extends RuntimeException {
  private final ROList<String> valid;
  private final ROList<String> full;

  public InvalidPath(ROList<String> valid, ROList<String> full) {
	  this.valid = valid;
	  this.full = full;
	}

  @Override
  public String toString() {
    return String.format("valid %s, full %s", valid, full);
  }
}
