package com.zarbosoft.merman.webview.serialization;

import java.util.List;

public interface JsonWriteState {
    void value(Object value);

    void key(String s);
}
