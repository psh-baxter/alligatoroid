package com.zarbosoft.alligatoroid.compiler.jvmshared;

import com.zarbosoft.rendaw.common.ROMap;

public class DynamicClassLoader extends ClassLoader {
    public static DynamicClassLoader instance = new DynamicClassLoader(DynamicClassLoader.class.getClassLoader());
    private ROMap<String, byte[]> bytecodes = null;

    private DynamicClassLoader(ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (bytecodes != null) {
            byte[] found = bytecodes.getOpt(name);
            if (found != null) {
                return defineClass(name, found, 0, found.length);
            }
        }
        return super.loadClass(name);
    }

    public synchronized static Class loadTree(String root, ROMap<String, byte[]> bytecodes) {
        //System.gc();
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        cl.bytecodes = bytecodes;
        Class out;
        try {
            out = cl.loadClass(root);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        cl.bytecodes = null;
        return out;
    }

}

