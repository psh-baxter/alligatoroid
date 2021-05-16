package com.zarbosoft.merman.core;

import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;

public class InvalidPath extends RuntimeException {
  private final ROList<String> valid;
  private final ROList<String> full;

  public InvalidPath(ROList<String> valid, ROList<String> full) {
	  this.valid = valid;
	  this.full = full;
	}

  @Override
  public String toString() {
    return Format.format("valid %s, full %s", valid.inner_(), full.inner_());
  }
}
