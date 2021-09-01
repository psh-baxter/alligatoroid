package com.zarbosoft.alligatoroid.compiler.cache;

import com.zarbosoft.alligatoroid.compiler.CompilationContext;
import com.zarbosoft.luxem.write.Writer;

import java.io.OutputStream;
import java.nio.file.Files;

public class CacheSerializer {
  public void serializeSubValue(CompilationContext context, Writer writer, Object value) {
    String key;
    if (value.getClass() == String.class) {
      writer.type("string").primitive((String) value);
    } else if (value.getClass() == Integer.class) {
      writer.type("int").primitive(((Integer) value).toString());
    } else if ((key = CompilationContext.builtinMapReverse.get(value)) != null) {
      writer.type("builtin").primitive(key);
    } else {
      key = context.loadedCacheReverse.getOpt(value);
      if (key == null) {
        key = serializeValue(value);
      }
      writer.type("cache").primitive(key);
    }
  }

  public String serializeValue(CompilationContext context, Object value) {
    context.nextObjectCachePath(moduleCachePath, false);
    String key = cache.getOutput();
    try (OutputStream stream = Files.newOutputStream(keyPath)) {
      Writer writer = new com.zarbosoft.luxem.write.Writer(stream, (byte) ' ', 4);
      if (builtinTypes.contains(value.getClass())) {
        writer.type("builtin").recordBegin();
        writer.recordEnd();
      } else {
        writer.type("output").arrayBegin();
        writer.primitive(cacheObjects.get(cacheTypes.get(value.getClass())));
        ((CacheSerializable) value).cacheSerialize(writer);
        writer.arrayEnd();
      }
    }
    return key;
  }
}
