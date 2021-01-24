package com.zarbosoft.merman.standalone.serialization;

import com.zarbosoft.merman.editor.backevents.BackEvent;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.JFalseEvent;
import com.zarbosoft.merman.editor.backevents.JFloatEvent;
import com.zarbosoft.merman.editor.backevents.JIntEvent;
import com.zarbosoft.merman.editor.backevents.JTrueEvent;

public abstract class JSONPath {

  public JSONPath parent;

  public abstract JSONPath type();

  public JSONPath push(final BackEvent e) {
    if (e.getClass() == EArrayOpenEvent.class) {
      return new JSONArrayPath(value());
    } else if (e.getClass() == EArrayCloseEvent.class) {
      return pop();
    } else if (e.getClass() == EObjectOpenEvent.class) {
      return new JSONObjectPath(value());
    } else if (e.getClass() == EObjectCloseEvent.class) {
      return pop();
    } else if (e.getClass() == EKeyEvent.class) {
      return key(((EKeyEvent) e).value);
    } else if (e.getClass() == EPrimitiveEvent.class) {
      return value();
    } else if (e.getClass() == JFalseEvent.class) {
      return value();
    } else if (e.getClass() == JTrueEvent.class) {
      return value();
    } else if (e.getClass() == JIntEvent.class) {
      return value();
    } else if (e.getClass() == JFloatEvent.class) {
      return value();
    } else throw new AssertionError(String.format("Unknown JSON event type [%s]", e.getClass()));
  }

  public abstract JSONPath value();

  public abstract JSONPath key(String data);

  public JSONPath pop() {
    return parent;
  }
}
