package com.zarbosoft.merman.webview.serialization;

import java.util.HashMap;
import java.util.Map;

public class JsonWriteRecordState implements com.zarbosoft.merman.webview.serialization.JsonWriteState {
    public final Map value = new HashMap();
    private String key;

    @Override
    public void value(Object value) {
        this.value.put(key, value);
    }

    @Override
    public void key(String s) {
        key = s;
    }
}
