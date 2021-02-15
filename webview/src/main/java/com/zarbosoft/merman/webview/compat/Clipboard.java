package com.zarbosoft.merman.webview.compat;

import elemental2.core.JsArray;
import elemental2.dom.DataTransfer;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class Clipboard {
  @JsMethod
  public native void write(JsArray<ClipboardItem> items);
  @JsMethod
  public native void writeText(String string);

  @JsMethod
  public native Promise<DataTransfer> read();
  @JsMethod
  public native Promise<String> readText();
}
