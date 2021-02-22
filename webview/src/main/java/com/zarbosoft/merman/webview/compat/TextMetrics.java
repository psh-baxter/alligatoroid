package com.zarbosoft.merman.webview.compat;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class TextMetrics extends elemental2.dom.TextMetrics {
    @JsProperty
    public double actualBoundingBoxAscent;
    @JsProperty
    public double actualBoundingBoxDescent;
    @JsProperty
    public double fontBoundingBoxAscent;
    @JsProperty
    public double fontBoundingBoxDescent;
}
