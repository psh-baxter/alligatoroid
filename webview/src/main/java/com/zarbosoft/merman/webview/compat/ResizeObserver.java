package com.zarbosoft.merman.webview.compat;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ResizeObserver {
  @JsFunction
  public
  interface ResizeCallbackFn {
    void onInvoke();
  }

  public ResizeObserver(ResizeCallbackFn cb) {}

  public native void observe(Element e);
}
