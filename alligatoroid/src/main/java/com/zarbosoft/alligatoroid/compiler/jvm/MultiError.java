package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.rendaw.common.TSList;

public class MultiError extends RuntimeException {
    private final TSList<Error> errors;

    public MultiError(TSList<Error> errors) {
        this.errors = errors;
    }
}
