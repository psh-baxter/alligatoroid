package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.luxem.write.Writer;

import java.nio.file.Path;

public class LocalModuleId implements ModuleId {
  public final Path path;

  public LocalModuleId(Path path) {
    this.path = path;
  }

  @Override
  public void serialize(Writer writer) {
    writer.type("local").recordBegin().key("path").primitive(path.toString()).recordEnd();
  }
}
