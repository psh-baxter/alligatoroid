package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Font;
import def.dom.CanvasRenderingContext2D;
import def.dom.Globals;
import jsweet.lang.Interface;
import jsweet.util.StringTypes;

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
        Globals.window.document.createElement(StringTypes.canvas).getContext(StringTypes._2d);
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

  @Interface
  public static class TextMetrics {
    public double width;
    public double fontBoundingBoxAscent;
    public double fontBoundingBoxDescent;
  }
}
