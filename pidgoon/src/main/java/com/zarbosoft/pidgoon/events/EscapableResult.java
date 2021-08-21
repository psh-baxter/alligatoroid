package com.zarbosoft.pidgoon.events;

public class EscapableResult<T> {
    public final boolean started;
    public final boolean completed;
    public final T value;

    public EscapableResult(boolean started, boolean completed, T value) {
        this.started = started;
        this.completed = completed;
        this.value = value;
    }
}
