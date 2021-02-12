package com.zarbosoft.merman.webview.compat;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class ClipboardItem {
  @JsConstructor
  public ClipboardItem(JsPropertyMap<Object> data) {}
}
