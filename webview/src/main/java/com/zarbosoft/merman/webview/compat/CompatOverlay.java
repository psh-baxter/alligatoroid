package com.zarbosoft.merman.webview.compat;

import elemental2.core.JsObject;
import elemental2.dom.DataTransfer;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;

import static jsinterop.annotations.JsPackage.GLOBAL;

public class CompatOverlay {
  @JsMethod(namespace = GLOBAL)
  public static native JsObject getSymbol(JsObject o, Object s);

  @JsMethod(namespace = GLOBAL)
  public static native void mmCopy(String mime, String data);

  @JsMethod(namespace = GLOBAL)
  public static native void mmCopyText(String text);

  @JsMethod(namespace = GLOBAL)
  public static native Promise<DataTransfer> mmUncopy();

  @JsMethod(namespace = GLOBAL)
  public static native Promise<String> mmUncopyText();
}
