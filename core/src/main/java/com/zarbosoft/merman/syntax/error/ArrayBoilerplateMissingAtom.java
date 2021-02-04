package com.zarbosoft.merman.syntax.error;

public class ArrayBoilerplateMissingAtom extends BaseKVError{
    public ArrayBoilerplateMissingAtom(int boilerplateIndex) {
put("boilerplateIndex", boilerplateIndex);
    }

    @Override
    protected String description() {
        return "boilerplate entry is missing back atom spec in tree";
    }
}
