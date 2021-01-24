package com.zarbosoft.rendaw.common;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.zarbosoft.rendaw.common.Common.deleteTree;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class TemporaryDirectory implements Closeable {
  public final Path path;

  public TemporaryDirectory(final String prefix) {
    this.path = uncheck(() -> Files.createTempDirectory(prefix));
  }

  @Override
  public void close() throws IOException {
    deleteTree(this.path);
  }
}
