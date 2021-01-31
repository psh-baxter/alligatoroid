package com.zarbosoft.merman.webview.serialization;

import def.js.Map;

import java.util.List;

public class JsonWriteRecordState implements com.zarbosoft.merman.webview.serialization.JsonWriteState {
    public final Map value = new Map();
    private String key;

    @Override
    public void value(Object value) {
        this.value.set(key, value);
    }

    @Override
    public void key(String s) {
        key = s;
    }
}
