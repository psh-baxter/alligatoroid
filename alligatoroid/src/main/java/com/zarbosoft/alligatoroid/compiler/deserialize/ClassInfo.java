package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;

import java.lang.reflect.Constructor;

public class ClassInfo {
    final String luxemType;
    Constructor constructor;
    ROMap<String, StatePrototype> fields;
    ROList<String> argOrder;

    public ClassInfo(String luxemType) {
        this.luxemType = luxemType;
    }
}
