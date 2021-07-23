package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class NonexistentDefaultSelection extends BaseKVError{
    public NonexistentDefaultSelection(String field) {
        put("defaultSelection", field);
    }

    @Override
    protected String description() {
        return "field specified for default selection doesn't exist";
    }
}
