package com.zarbosoft.merman.editor.serialization;

public interface EventConsumer {
    void primitive(String value);

    void type(String value);

    void arrayBegin();

    void arrayEnd();

    void recordBegin();

    void recordEnd();

    void key(String s);

    void jsonInt(String value);

    void jsonFloat(String value);

    void jsonTrue();

    void jsonFalse();

    void jsonNull();
}
