package com.zarbosoft.merman.webview.compat.intl;

import elemental2.core.JsArray;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = "Intl")
public class Segmenter {
  public Segmenter(String lang, JsPropertyMap<Object> options) {}

  public native JsArray<Segment> segment(String text);
}
