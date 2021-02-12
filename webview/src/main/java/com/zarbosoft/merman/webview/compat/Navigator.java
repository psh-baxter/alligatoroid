package com.zarbosoft.merman.webview.compat;

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true)
public class Navigator extends elemental2.dom.Navigator {
    public Clipboard clipboard;
}
