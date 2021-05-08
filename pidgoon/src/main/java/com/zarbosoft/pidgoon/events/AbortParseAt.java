package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.errors.AbortParse;

public class AbortParseAt extends RuntimeException {
    public final Object at;
    public final AbortParse e;

    public AbortParseAt(Object at, AbortParse e) {
        this.at = at;
        this.e = e;
    }
}
