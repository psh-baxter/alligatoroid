package com.zarbosoft.luxem.read;

import java.io.ByteArrayOutputStream;

/** Extension for RawReader that buffers primitives, keys, and types. */
public abstract class BufferedReader extends Reader {
  private final Integer chunked;
  boolean sent = false;
  private ByteArrayOutputStream top = new ByteArrayOutputStream();

  public BufferedReader() {
    this(null);
  }

  public BufferedReader(final Integer newChunked) {
    this.chunked = newChunked;
  }

  @Override
  protected void eatTypeEnd() {
    if (!sent || top.size() > 0) {
      eatType(top.toByteArray());
      top.reset();
    }
  }

  @Override
  protected void eatType(byte b) {
    top.write(b);
    if (chunked != null && top.size() >= chunked) {
      eatType(top.toByteArray());
      top.reset();
      sent = true;
    }
  }

  protected abstract void eatType(byte[] b);

  @Override
  protected void eatTypeBegin() {
    top = new ByteArrayOutputStream();
    sent = false;
  }

  @Override
  protected void eatKey(byte b) {
    top.write(b);
    if (chunked != null && top.size() >= chunked) {
      eatKey(top.toByteArray());
      top.reset();
      sent = true;
    }
  }

  protected abstract void eatKey(byte[] b);

  @Override
  protected void eatKeyBegin() {
    top = new ByteArrayOutputStream();
    sent = false;
  }

  @Override
  protected void eatKeyEnd() {
    if (!sent || top.size() > 0) {
      eatKey(top.toByteArray());
      top.reset();
    }
  }

  @Override
  protected void eatPrimitive(byte b) {
    top.write(b);
    if (chunked != null && top.size() >= chunked) {
      eatPrimitive(top.toByteArray());
      top.reset();
      sent = true;
    }
  }

  protected abstract void eatPrimitive(byte[] b);

  @Override
  protected void eatPrimitiveBegin() {
    top = new ByteArrayOutputStream();
    sent = false;
  }

  @Override
  protected void eatPrimitiveEnd() {
    if (!sent || top.size() > 0) {
      eatPrimitive(top.toByteArray());
      top.reset();
    }
  }
}
