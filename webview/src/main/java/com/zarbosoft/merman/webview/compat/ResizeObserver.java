package com.zarbosoft.merman.webview.compat;

import elemental2.dom.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ResizeObserver {
  public ResizeObserver(Runnable cb) {}

  public void observe(Element e) {}
}
