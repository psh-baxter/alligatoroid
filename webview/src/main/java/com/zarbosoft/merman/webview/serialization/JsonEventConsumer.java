package com.zarbosoft.merman.webview.serialization;

import def.js.JSON;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

public class JsonEventConsumer implements JSEventConsumer {
  Deque<JsonWriteState> stack = new ArrayDeque<>();

  {
    stack.add(new JsonWriteArrayState());
  }

  @Override
  public byte[] resultOne() {
    return JSON.stringify(
            ((JsonWriteArrayState) stack.getLast()).value.get(0), (BiFunction) null, "    ")
        .getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] resultMany() {
    return JSON.stringify(((JsonWriteArrayState) stack.getLast()).value, (BiFunction) null, "    ")
        .getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void primitive(final String value) {
    stack.getLast().value(value);
  }

  @Override
  public void type(final String value) {
    throw new AssertionError();
  }

  @Override
  public void arrayBegin() {
    stack.add(new JsonWriteArrayState());
  }

  @Override
  public void arrayEnd() {
    JsonWriteArrayState array = (JsonWriteArrayState) stack.removeLast();
    stack.getLast().value(array.value);
  }

  @Override
  public void recordBegin() {
    stack.add(new JsonWriteRecordState());
  }

  @Override
  public void recordEnd() {
    JsonWriteRecordState record = (JsonWriteRecordState) stack.removeLast();
    stack.getLast().value(record.value);
  }

  @Override
  public void key(final String s) {
    stack.getLast().key(s);
  }

  @Override
  public void jsonInt(final String value) {
    stack.getLast().value(Integer.parseInt(value));
  }

  @Override
  public void jsonFloat(final String value) {
    stack.getLast().value(Float.parseFloat(value));
  }

  @Override
  public void jsonTrue() {
    stack.getLast().value(true);
  }

  @Override
  public void jsonFalse() {
    stack.getLast().value(false);
  }

  @Override
  public void jsonNull() {
    stack.getLast().value(null);
  }
}
