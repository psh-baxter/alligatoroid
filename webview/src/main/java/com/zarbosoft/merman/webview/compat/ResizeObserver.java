package com.zarbosoft.merman.webview.compat;

import elemental2.dom.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ResizeObserver {
  @JsConstructor
  public ResizeObserver(ResizeCallbackFn cb) {}

  @JsMethod
  public native void observe(Element e);

  @JsFunction
  public interface ResizeCallbackFn {
    void onInvoke();
  }
}
