package com.zarbosoft.merman.webview.serialization;

import com.zarbosoft.rendaw.common.Assertion;

import java.util.ArrayList;
import java.util.List;

public class JsonWriteArrayState implements com.zarbosoft.merman.webview.serialization.JsonWriteState {
    public List value = new ArrayList();

    @Override
    public void value(Object value) {
        this.value.add(value);
    }

    @Override
    public void key(String s) {
        throw new Assertion();
    }
}
