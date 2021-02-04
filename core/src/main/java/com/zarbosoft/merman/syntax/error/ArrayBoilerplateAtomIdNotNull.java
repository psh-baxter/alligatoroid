package com.zarbosoft.merman.syntax.error;

public class ArrayBoilerplateAtomIdNotNull extends BaseKVError{
    public ArrayBoilerplateAtomIdNotNull(String boilerplate) {
        put("boilerplate", boilerplate);
    }

    @Override
    protected String description() {
        return "boilerplate atom id is not null";
    }
}
