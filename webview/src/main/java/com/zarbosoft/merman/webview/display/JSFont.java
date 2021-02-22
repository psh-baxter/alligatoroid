package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.webview.compat.TextMetrics;
import com.zarbosoft.rendaw.common.Format;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;

public class JSFont implements Font {
  public final String name;
  public final int size;
  private final double ascent;
  private final double descent;

  public JSFont(String name, int size) {
    this.name = name == null ? "monospace" : name;
    this.size = size;
    TextMetrics basis = measure("W");
    ascent = basis.fontBoundingBoxAscent;
    descent = basis.fontBoundingBoxDescent;
  }

  public String cssString() {
    return Format.format("%spx %s", size, name);
  }

  public TextMetrics measure(String text) {
    HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
    CanvasRenderingContext2D context = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
    context.setFont(cssString());
    TextMetrics out = (TextMetrics) context.measureText(text);
    return out;
  }

  @Override
  public double getAscent() {
    return ascent;
  }

  @Override
  public double getDescent() {
    return descent;
  }

  @Override
  public double getWidth(String text) {
    return (int) measure(text).width;
  }

  @Override
  public int getIndexAtConverse(String text, double converse) {
    double last = 0;
    for (int i = 0; i < text.length(); ++i) {
      double next = measure(text.substring(0, i)).width;
      if (next > converse) return (int) last;
      last = next;
    }
    return (int) last;
  }
}
