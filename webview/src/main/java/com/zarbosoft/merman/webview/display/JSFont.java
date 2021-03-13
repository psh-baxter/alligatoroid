package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.I18nEngine;
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
    TextMetrics basis = measurer().measure("W");
    ascent = basis.fontBoundingBoxAscent;
    descent = basis.fontBoundingBoxDescent;
  }

  public String cssString() {
    return Format.format("%spx %s", size, name);
  }

  @Override
  public JSMeasurer measurer() {
    return new JSMeasurer();
  }

  @Override
  public double getAscent() {
    return ascent;
  }

  @Override
  public double getDescent() {
    return descent;
  }

  public class JSMeasurer implements Measurer {
    private CanvasRenderingContext2D context;

    {
      HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
      context = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
      context.setFont(cssString());
    }

    public TextMetrics measure(String text) {
      return (TextMetrics) context.measureText(text);
    }

    @Override
    public double getWidth(String text) {
      return measure(text).width;
    }

    @Override
    public int getIndexAtConverse(Context context, String text, double converse) {
      I18nEngine.Walker walker = context.i18n.glyphWalker(text);
      double lastTextConverse = 0;
      int lastIndex = 0;
      int index = 0;
      while (true) {
        index = walker.following(index);
        if (index == I18nEngine.DONE) break;
        double textConverse = measure(text.substring(0, index)).width;
        if ((converse - lastTextConverse) / (textConverse - lastTextConverse) < 0.5) break;
        lastTextConverse = textConverse;
        lastIndex = index;
      }
      return lastIndex;
    }
  }
}
