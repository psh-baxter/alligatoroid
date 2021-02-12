package com.zarbosoft.merman.webview.compat.intl;

import elemental2.core.JsArray;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, name = "Intl.Segmenter", namespace = GLOBAL)
public class Segmenter {
  @JsConstructor
  public Segmenter(String lang, JsPropertyMap<Object> options) {}

  @JsMethod
  public native JsArray<Segment> segment(String text);
}
