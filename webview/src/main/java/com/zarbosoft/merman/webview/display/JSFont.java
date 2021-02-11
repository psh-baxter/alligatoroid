package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Font;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.TextMetrics;
import jsinterop.annotations.JsType;

public class JSFont implements Font {
  public final String name;
  public final int size;
  private final double ascent;
  private final double descent;

  public JSFont(String name, int size) {
    this.name = name;
    this.size = size;
    TextMetrics basis = measure("W");
    ascent = basis.fontBoundingBoxAscent;
    descent = basis.fontBoundingBoxDescent;
  }

  public String cssString() {
    return String.format("%s %dpt", name, size);
  }

  public TextMetrics measure(String text) {
    CanvasRenderingContext2D context =
        (CanvasRenderingContext2D)
            (Object)
                ((HTMLCanvasElement) DomGlobal.document.createElement("canvas")).getContext("2d");
    context.font = cssString();
    return (TextMetrics) (Object) context.measureText(text);
  }

  @Override
  public int getAscent() {
    return (int) ascent;
  }

  @Override
  public int getDescent() {
    return (int) descent;
  }

  @Override
  public int getWidth(String text) {
    return (int) measure(text).width;
  }

  @Override
  public int getIndexAtConverse(String text, int converse) {
    double last = 0;
    for (int i = 0; i < text.length(); ++i) {
      double next = measure(text.substring(0, i)).width;
      if (next > converse) return (int) last;
      last = next;
    }
    return (int) last;
  }

  public static class TextMetrics extends elemental2.dom.TextMetrics {
    public double fontBoundingBoxAscent;
    public double fontBoundingBoxDescent;
  }
}
