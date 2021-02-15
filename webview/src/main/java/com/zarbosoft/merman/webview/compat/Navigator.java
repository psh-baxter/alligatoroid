package com.zarbosoft.merman.webview.compat;

import elemental2.core.JsObject;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class Navigator extends elemental2.dom.Navigator {
  @JsProperty
  public Clipboard clipboard;
}
