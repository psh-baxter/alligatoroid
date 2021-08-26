package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.luxem.write.Writer;

public final class Location implements Serializable {
  public final ModuleId module;
  public final int id;

  public Location(ModuleId module, int id) {
    this.module = module;
    this.id = id;
  }

  @Override
  public void serialize(Writer writer) {
    writer.recordBegin().key("module");
    module.serialize(writer);
    writer.key("id").primitive(Integer.toString(id)).recordEnd();
  }
}
