package com.zarbosoft.merman.webview.compat.intl;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class Segment {
  @JsProperty
  public String segment;
  @JsProperty
  public int index;
  @JsProperty
  boolean wordLike;
}
