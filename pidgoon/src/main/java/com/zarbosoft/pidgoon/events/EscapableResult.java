package com.zarbosoft.pidgoon.events;

public class EscapableResult<T> {
    public final boolean escaped;
    public final T value;

    public EscapableResult(boolean escaped, T value) {
        this.escaped = escaped;
        this.value = value;
    }
}
