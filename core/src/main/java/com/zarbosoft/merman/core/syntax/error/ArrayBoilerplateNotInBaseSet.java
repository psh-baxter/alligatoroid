package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;

public class ArrayBoilerplateNotInBaseSet extends BaseKVError{
    public ArrayBoilerplateNotInBaseSet(SyntaxPath typePath, String boilerplate, String splayedType) {
        put("typePath", typePath);
        put("boilerplate", boilerplate);
        put("splayedType", splayedType);
    }

    @Override
    protected String description() {
        return "boilerplate splayed type is not in array base type specification";
    }
}
