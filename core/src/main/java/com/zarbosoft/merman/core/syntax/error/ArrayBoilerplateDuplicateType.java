package com.zarbosoft.merman.core.syntax.error;

public class ArrayBoilerplateDuplicateType extends BaseKVError{
    public ArrayBoilerplateDuplicateType(String type) {
        put("type", type);
    }

    @Override
    protected String description() {
        return "duplicate boilerplate entries with type";
    }
}
