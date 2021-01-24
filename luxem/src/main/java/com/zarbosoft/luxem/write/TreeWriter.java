package com.zarbosoft.luxem.write;

import com.zarbosoft.luxem.tree.Typed;
import com.zarbosoft.rendaw.common.Assertion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TreeWriter {
  public static void write(final OutputStream stream, final List<?> tree) throws IOException {
    write(stream, false, (byte) 0, 0, tree);
  }

  public static void write(
      final OutputStream stream,
      final byte indentByte,
      final int indentMultiple,
      final List<?> tree)
      throws IOException {
    write(stream, true, indentByte, indentMultiple, tree);
  }

  public static String write(final List<?> tree) {
    return write(false, (byte) 0, 0, tree);
  }

  public static String write(final byte indentByte, final int indentMultiple, final List<?> tree) {
    return write(true, indentByte, indentMultiple, tree);
  }

  public static void write(
      final OutputStream stream,
      final boolean pretty,
      final byte indentByte,
      final int indentMultiple,
      final List<?> tree)
      throws IOException {
    final Writer writer = new Writer(stream, pretty, indentByte, indentMultiple);
    for (final Object o : tree) {
      writeNode(writer, o);
    }
  }

  public static String write(
      final boolean pretty, final byte indentByte, final int indentMultiple, final List<?> tree) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      write(out, pretty, indentByte, indentMultiple, tree);
    } catch (IOException e) {
      throw new Assertion(); // Shouldn't happen, really
    }
    return out.toString();
  }

  private static void writeNode(final Writer writer, final Object o) throws IOException {
    if (o instanceof Map) {
      writer.recordBegin();
      for (final Map.Entry<Object, Object> e : ((Map<Object, Object>) o).entrySet()) {
        writer.key(e.getKey().toString());
        writeNode(writer, e.getValue());
      }
      writer.recordEnd();
    } else if (o instanceof List) {
      writer.arrayBegin();
      for (final Object c : (List) o) {
        writeNode(writer, c);
      }
      writer.arrayEnd();
    } else if (o instanceof Typed) {
      writer.type(((Typed) o).name);
      writeNode(writer, ((Typed) o).value);
    } else {
      writer.primitive(o.toString().getBytes(StandardCharsets.UTF_8));
    }
  }
}
