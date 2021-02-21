package com.zarbosoft.merman.webview.serialization;

import com.zarbosoft.rendaw.common.TSList;
import elemental2.core.JSONType;
import elemental2.core.JsNumber;

import static elemental2.core.Global.JSON;

public class JsonEventConsumer implements JSEventConsumer {
  TSList<JsonWriteState> stack = new TSList<>();

  {
    stack.add(new JsonWriteArrayState());
  }

  @Override
  public String resultOne() {
    return JSON.stringify(
        ((JsonWriteArrayState) stack.last()).value.getAt(0),
        (JSONType.StringifyReplacerFn) null,
        "    ");
  }

  @Override
  public String resultMany() {
    return JSON.stringify(
        ((JsonWriteArrayState) stack.last()).value, (JSONType.StringifyReplacerFn) null, "    ");
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
    if ("true".equals(value)) stack.last().value(true);
    else if ("false".equals(value)) stack.last().value(false);
    else if ("null".equals(value)) stack.last().value(null);
    else if (!value.contains(".")) stack.last().value(JsNumber.parseInt(value, 10));
    else stack.last().value(JsNumber.parseFloat(value));
  }
}
