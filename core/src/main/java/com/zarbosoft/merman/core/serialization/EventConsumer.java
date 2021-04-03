package com.zarbosoft.merman.core.serialization;

public interface EventConsumer {
    void primitive(String value);

    void type(String value);

    void arrayBegin();

    void arrayEnd();

    void recordBegin();

    void recordEnd();

    void key(String s);

    void jsonSpecialPrimitive(String value);
}
