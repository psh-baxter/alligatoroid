package com.zarbosoft.alligatoroid.compiler.jvm;

public interface JVMDataType extends JVMType{
    JVMType getField(Object key);

    String jvmDesc();
}
