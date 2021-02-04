package com.zarbosoft.luxem.read.path;

import com.zarbosoft.luxem.events.LArrayCloseEvent;
import com.zarbosoft.luxem.events.LArrayOpenEvent;
import com.zarbosoft.luxem.events.LKeyEvent;
import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.luxem.events.LRecordCloseEvent;
import com.zarbosoft.luxem.events.LRecordOpenEvent;
import com.zarbosoft.luxem.events.LTypeEvent;
import com.zarbosoft.luxem.events.LuxemEvent;

public abstract class LuxemPath {

  public LuxemPath parent;

  public abstract LuxemPath unkey();

  public LuxemPath push(final LuxemEvent e) {
    if (e.getClass() == LArrayOpenEvent.class) {
      return new LuxemArrayPath(value());
    } else if (e.getClass() == LArrayCloseEvent.class) {
      return pop();
    } else if (e.getClass() == LRecordOpenEvent.class) {
      return new LuxemObjectPath(value());
    } else if (e.getClass() == LRecordCloseEvent.class) {
      return pop();
    } else if (e.getClass() == LKeyEvent.class) {
      return key(((LKeyEvent) e).value);
    } else if (e.getClass() == LTypeEvent.class) {
      return type();
    } else if (e.getClass() == LPrimitiveEvent.class) {
      return value();
    } else throw new AssertionError(String.format("Unknown luxem event type [%s]", e.getClass()));
  }

  public abstract LuxemPath value();

  public abstract LuxemPath key(String data);

  public abstract LuxemPath type();

  public LuxemPath pop() {
    return parent;
  }
}
