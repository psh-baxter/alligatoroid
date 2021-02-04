package com.zarbosoft.pidgoon.events;

public interface EventSink<E extends EventSink<?>> {
  public E push(final Event event, final Object at);
}
