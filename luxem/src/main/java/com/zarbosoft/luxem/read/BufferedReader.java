package com.zarbosoft.luxem.read;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Extension for RawReader that buffers primitives, keys, and types. */
public abstract class BufferedReader extends Reader {
  private ByteArrayOutputStream top = new ByteArrayOutputStream();

  @Override
  protected void eatTypeEnd() {
    eatType(new String(top.toByteArray(), StandardCharsets.UTF_8));
    top.reset();
  }

  @Override
  protected void eatType(byte b) {
    top.write(b);
  }

  protected abstract void eatType(String value);

  @Override
  protected void eatTypeBegin() {}

  @Override
  protected void eatKey(byte b) {
    top.write(b);
  }

  protected abstract void eatKey(String value);

  @Override
  protected void eatKeyBegin() {}

  @Override
  protected void eatKeyEnd() {
    eatKey(new String(top.toByteArray(), StandardCharsets.UTF_8));
    top.reset();
  }

  @Override
  protected void eatPrimitive(byte b) {
    top.write(b);
  }

  protected abstract void eatPrimitive(String value);

  @Override
  protected void eatPrimitiveBegin() {
    top = new ByteArrayOutputStream();
  }

  @Override
  protected void eatPrimitiveEnd() {
    eatPrimitive(new String(top.toByteArray(), StandardCharsets.UTF_8));
    top.reset();
  }
}
