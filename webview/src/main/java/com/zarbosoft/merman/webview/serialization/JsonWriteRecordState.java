package com.zarbosoft.merman.webview.serialization;

import elemental2.core.JsObject;
import jsinterop.base.JsPropertyMap;

import java.util.HashMap;

public class JsonWriteRecordState implements com.zarbosoft.merman.webview.serialization.JsonWriteState {
    public final JsPropertyMap value = (JsPropertyMap) new JsObject();
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
