package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.editor.backevents.BackEvent;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.JSpecialPrimitiveEvent;

public abstract class JSONPath {
  public JSONPath parent;

  public abstract JSONPath type();

  public JSONPath push(final BackEvent e) {
    if (e instanceof EArrayOpenEvent) {
      return new JSONArrayPath(value());
    } else if (e instanceof EArrayCloseEvent) {
      return pop();
    } else if (e instanceof EObjectOpenEvent) {
      return new JSONObjectPath(value());
    } else if (e instanceof EObjectCloseEvent) {
      return pop();
    } else if (e instanceof EKeyEvent) {
      return key(((EKeyEvent) e).value);
    } else if (e instanceof EPrimitiveEvent) {
      return value();
    } else if (e instanceof JSpecialPrimitiveEvent) {
      return value();
    } else throw new AssertionError(String.format("Unknown JSON event type [%s]", e));
  }

  public abstract JSONPath value();

  public abstract JSONPath key(String data);

  public JSONPath pop() {
    return parent;
  }
}
