package com.zarbosoft.pidgoon.events;

public class EscapableResult<T> {
    public final boolean completed;
    public final T value;

    public EscapableResult(boolean completed, T value) {
        this.completed = completed;
        this.value = value;
    }
}
