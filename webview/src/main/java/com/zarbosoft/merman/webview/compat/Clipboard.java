package com.zarbosoft.merman.webview.compat;

import def.dom.DataTransfer;
import def.js.Promise;

public interface Clipboard {
    Promise<DataTransfer> read();
    Promise<java.lang.String> readText();
    Promise<java.lang.Object> write(Object[] items);
    Promise<java.lang.Object> writeText(java.lang.String text);
}
