package com.zarbosoft.luxem.read;

import java.io.InputStream;
import java.util.List;

public class TreeReader extends StackReader {
  public List read(final InputStream stream) {
    return read(stream, new ArrayState());
  }
}
