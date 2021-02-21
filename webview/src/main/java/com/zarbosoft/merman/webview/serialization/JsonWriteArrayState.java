package com.zarbosoft.merman.webview.serialization;

import com.zarbosoft.rendaw.common.Assertion;
import elemental2.core.JsArray;

import java.util.ArrayList;
import java.util.List;

public class JsonWriteArrayState implements com.zarbosoft.merman.webview.serialization.JsonWriteState {
    public JsArray value = new JsArray();

    @Override
    public void value(Object value) {
        this.value.push(value);
    }

    @Override
    public void key(String s) {
        throw new Assertion();
    }
}
