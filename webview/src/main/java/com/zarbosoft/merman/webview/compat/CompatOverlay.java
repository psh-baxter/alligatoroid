package com.zarbosoft.merman.webview.compat;

import elemental2.core.JsObject;
import elemental2.core.Symbol;
import jsinterop.annotations.JsMethod;

import static jsinterop.annotations.JsPackage.GLOBAL;

public class CompatOverlay {
    @JsMethod(namespace = GLOBAL)
    public native static JsObject getSymbol(JsObject o, Object s);
}
