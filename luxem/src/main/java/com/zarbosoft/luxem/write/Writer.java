package com.zarbosoft.luxem.write;

import com.zarbosoft.luxem.events.LArrayCloseEvent;
import com.zarbosoft.luxem.events.LArrayOpenEvent;
import com.zarbosoft.luxem.events.LKeyEvent;
import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.luxem.events.LRecordCloseEvent;
import com.zarbosoft.luxem.events.LRecordOpenEvent;
import com.zarbosoft.luxem.events.LTypeEvent;
import com.zarbosoft.luxem.events.LuxemEvent;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSMap;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Writer {
  private static final ROMap<Byte, Byte> quotedKeyEscapes =
      escapeMap()
          .put((byte) '"', (byte) '"')
          .put((byte) '\n', (byte) 'n')
          .put((byte) '\t', (byte) 't')
          .put((byte) '\r', (byte) 'r')
          ;
  private static final ROMap<Byte, Byte> typeEscapes =
      escapeMap()
          .put((byte) ')', (byte) ')')
          .put((byte) '\n', (byte) 'n')
          .put((byte) '\t', (byte) 't')
          .put((byte) '\r', (byte) 'r')
          ;
  private static final ROMap<Byte, Byte> quotedPrimitiveEscapes =
      escapeMap()
          .put((byte) '"', (byte) '"')
          .put((byte) '\n', (byte) 'n')
          .put((byte) '\t', (byte) 't')
          .put((byte) '\r', (byte) 'r')
          ;
  private final boolean pretty;
  private final byte indentByte;
  private final int indentMultiple;
  private final OutputStream stream;
  private final Deque<State> states = new ArrayDeque<>();
  private boolean first = true;

  public Writer(final OutputStream stream) {
    this(stream, false, (byte) 0, 0);
  }

  public Writer(
      final OutputStream stream,
      final boolean pretty,
      final byte indentByte,
      final int indentMultiple) {
    this.stream = stream;
    this.pretty = pretty;
    this.indentByte = indentByte;
    this.indentMultiple = indentMultiple;
    states.addLast(State.ARRAY);
  }

  public Writer(final OutputStream stream, final byte indentByte, final int indentMultiple) {
    this(stream, true, indentByte, indentMultiple);
  }

  private static TSMap<Byte, Byte> escapeMap() {
    return new TSMap<Byte, Byte>().put((byte) '\\', (byte) '\\');
  }

  private static void escape(
      final OutputStream stream, final byte[] bytes, final ROMap<Byte, Byte> escapes) {
    uncheck(
        () -> {
          int lastEscape = 0;
          for (int i = 0; i < bytes.length; ++i) {
            final Byte key = escapes.getOpt(bytes[i]);
            if (key == null) continue;
            stream.write(bytes, lastEscape, i - lastEscape);
            stream.write('\\');
            stream.write(key);
            lastEscape = i + 1;
          }
          stream.write(bytes, lastEscape, bytes.length - lastEscape);
        });
  }

  public Writer emit(final LuxemEvent event) {
    final Class<?> k = event.getClass();
    if (false) {
      return null; // dead code
    } else if (k == LPrimitiveEvent.class) {
      return primitive(((LPrimitiveEvent) event).value);
    } else if (k == LTypeEvent.class) {
      return type(((LTypeEvent) event).value);
    } else if (k == LArrayOpenEvent.class) {
      return arrayBegin();
    } else if (k == LArrayCloseEvent.class) {
      return arrayEnd();
    } else if (k == LRecordOpenEvent.class) {
      return recordBegin();
    } else if (k == LRecordCloseEvent.class) {
      return recordEnd();
    } else if (k == LKeyEvent.class) {
      return key(((LKeyEvent) event).value);
    } else {
      throw new Assertion();
    }
  }

  public Writer type(final String value) {
    return type(value.getBytes(StandardCharsets.UTF_8));
  }

  public Writer primitive(final String value) {
    return primitive(value.getBytes(StandardCharsets.UTF_8));
  }

  public Writer key(final String key) {
    return key(key.getBytes(StandardCharsets.UTF_8));
  }

  private void valueBegin() {
    if (states.peekLast() == State.PREFIXED) {
      states.pollLast();
    } else if (first) {
      first = false;
    } else if (pretty) {
      uncheck(
          () -> {
            stream.write('\n');
          });
      indent();
    }
  }

  private void indent() {
    if (!pretty) return;
    uncheck(
        () -> {
          for (int i = 0; i < states.size() - 1; ++i)
            for (int j = 0; j < indentMultiple; ++j) stream.write(indentByte);
        });
  }

  public Writer recordBegin() {
    uncheck(
        () -> {
          valueBegin();
          stream.write((byte) '{');
          states.addLast(State.RECORD);
        });
    return this;
  }

  public Writer recordEnd() {
    uncheck(
        () -> {
          states.pollLast();
          if (pretty) {
            stream.write('\n');
            indent();
          }
          stream.write((byte) '}');
          stream.write((byte) ',');
        });
    return this;
  }

  public Writer arrayBegin() {
    uncheck(
        () -> {
          valueBegin();
          stream.write((byte) '[');
          states.addLast(State.ARRAY);
        });
    return this;
  }

  public Writer arrayEnd() {
    uncheck(
        () -> {
          states.pollLast();
          if (pretty) {
            stream.write('\n');
            indent();
          }
          stream.write((byte) ']');
          stream.write((byte) ',');
        });
    return this;
  }

  private boolean isAmbiguous(final byte b) {
    switch (b) {
      case '"':
      case ':':
      case ',':
      case '[':
      case ']':
      case '{':
      case '}':
      case '(':
      case ')':
      case ' ':
      case '\\':
      case '\n':
      case '\t':
      case '\r':
      case '*':
        return true;
      default:
        return false;
    }
  }

  public Writer key(final byte[] bytes) {
    if (pretty) {
      for (int i = 0; i < bytes.length; ++i) {
        if (isAmbiguous(bytes[i])) return quotedKey(bytes);
      }
      return shortKey(bytes);
    } else return quotedKey(bytes);
  }

  public Writer shortKey(final byte[] bytes) {
    shortKeyBegin();
    shortKeyChunk(bytes);
    shortKeyEnd();
    return this;
  }

  public Writer shortKeyBegin() {
    if (pretty) {
      uncheck(
          () -> {
            stream.write('\n');
          });
      indent();
    }
    return this;
  }

  private Writer shortKeyEnd() {
    uncheck(
        () -> {
          stream.write(':');
          if (pretty) stream.write(' ');
          states.addLast(State.PREFIXED);
        });
    return this;
  }

  public Writer shortKeyChunk(final byte[] bytes) {
    uncheck(
        () -> {
          stream.write(bytes);
        });
    return this;
  }

  public Writer quotedKey(final byte[] bytes) {
    quotedKeyBegin();
    quotedKeyChunk(bytes);
    quotedKeyEnd();
    return this;
  }

  public Writer quotedKeyBegin() {
    uncheck(
        () -> {
          if (pretty) {
            stream.write('\n');
            indent();
          }
          stream.write('"');
        });
    return this;
  }

  private Writer quotedKeyEnd() {
    uncheck(
        () -> {
          stream.write('"');
          stream.write(':');
          if (pretty) stream.write(' ');
          states.addLast(State.PREFIXED);
        });
    return this;
  }

  public Writer quotedKeyChunk(final byte[] bytes) {
    escape(stream, bytes, quotedKeyEscapes);
    return this;
  }

  public Writer type(final byte[] bytes) {
    typeBegin();
    typeChunk(bytes);
    typeEnd();
    return this;
  }

  public Writer typeBegin() {
    valueBegin();
    uncheck(
        () -> {
          stream.write('(');
        });
    return this;
  }

  public Writer typeEnd() {
    uncheck(
        () -> {
          stream.write(')');
          if (pretty) stream.write(' ');
          states.addLast(State.PREFIXED);
        });
    return this;
  }

  public Writer typeChunk(final byte[] bytes) {
    escape(stream, bytes, typeEscapes);
    return this;
  }

  public Writer primitive(final byte[] bytes) {
    if (pretty) {
      for (int i = 0; i < bytes.length; ++i) {
        if (isAmbiguous(bytes[i])) return quotedPrimitive(bytes);
      }
      return shortPrimitive(bytes);
    } else return quotedPrimitive(bytes);
  }

  public Writer shortPrimitive(final byte[] bytes) {
    shortPrimitiveBegin();
    shortPrimitiveChunk(bytes);
    shortPrimitiveEnd();
    return this;
  }

  public Writer shortPrimitiveBegin() {
    valueBegin();
    return this;
  }

  private Writer shortPrimitiveEnd() {
    uncheck(
        () -> {
          stream.write(',');
        });
    return this;
  }

  private Writer shortPrimitiveChunk(final byte[] bytes) {
    uncheck(
        () -> {
          stream.write(bytes);
        });
    return this;
  }

  public Writer quotedPrimitive(final byte[] bytes) {
    quotedPrimitiveBegin();
    quotedPrimitiveChunk(bytes);
    quotedPrimitiveEnd();
    return this;
  }

  public Writer quotedPrimitiveBegin() {
    valueBegin();
    uncheck(
        () -> {
          stream.write('"');
        });
    return this;
  }

  public Writer quotedPrimitiveEnd() {
    uncheck(
        () -> {
          stream.write('"');
          stream.write(',');
        });
    return this;
  }

  public Writer quotedPrimitiveChunk(final byte[] bytes) {
    escape(stream, bytes, quotedPrimitiveEscapes);
    return this;
  }

  private enum State {
    ARRAY,
    RECORD,
    PREFIXED,
  }
}
