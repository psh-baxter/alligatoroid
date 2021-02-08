package com.zarbosoft.merman.webview.serialization;

import com.zarbosoft.rendaw.common.TSList;
import def.js.JSON;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

public class JsonEventConsumer implements JSEventConsumer {
  TSList<JsonWriteState> stack = new TSList<>();

  {
    stack.add(new JsonWriteArrayState());
  }

  @Override
  public byte[] resultOne() {
    return JSON.stringify(
            ((JsonWriteArrayState) stack.last()).value.get(0), (BiFunction) null, "    ")
        .getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] resultMany() {
    return JSON.stringify(((JsonWriteArrayState) stack.last()).value, (BiFunction) null, "    ")
        .getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void primitive(final String value) {
    stack.last().value(value);
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
    stack.last().value(array.value);
  }

  @Override
  public void recordBegin() {
    stack.add(new JsonWriteRecordState());
  }

  @Override
  public void recordEnd() {
    JsonWriteRecordState record = (JsonWriteRecordState) stack.removeLast();
    stack.last().value(record.value);
  }

  @Override
  public void key(final String s) {
    stack.last().key(s);
  }

  @Override
  public void jsonSpecialPrimitive(String value) {
    stack.last().value(value);
  }
}
