package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.parse.Parse;

public interface EventSink<E extends EventSink<?>> {
  public E push(final Event event, final Object at);
}
