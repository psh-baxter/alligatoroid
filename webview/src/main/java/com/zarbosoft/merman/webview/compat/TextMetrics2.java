package com.zarbosoft.merman.webview.compat;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class TextMetrics2 {
    @JsProperty
    public double width;
    @JsProperty
    public double fontBoundingBoxAscent;
    @JsProperty
    public double fontBoundingBoxDescent;
}
