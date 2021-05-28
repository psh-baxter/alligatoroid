package com.zarbosoft.pidgoon.events;

public interface EventSink<E extends EventSink<?>> {
  public E push(final Object event, final Object at);
}
