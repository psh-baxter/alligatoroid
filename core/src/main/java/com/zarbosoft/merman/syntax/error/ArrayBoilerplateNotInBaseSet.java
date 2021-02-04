package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.editor.Path;

public class ArrayBoilerplateNotInBaseSet extends BaseKVError{
    public ArrayBoilerplateNotInBaseSet(Path typePath, String boilerplate, String splayedType) {
        put("typePath", typePath);
        put("boilerplate", boilerplate);
        put("splayedType", splayedType);
    }

    @Override
    protected String description() {
        return "boilerplate splayed type is not in array base type specification";
    }
}
