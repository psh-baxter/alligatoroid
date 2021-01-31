package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;
import def.dom.CanvasRenderingContext2D;
import def.dom.Globals;
import def.dom.HTMLCanvasElement;
import def.dom.HTMLElement;
import jsweet.util.StringTypes;

import static jsweet.util.Lang.union;

public class JSDrawing extends JSDisplayNode implements Drawing {
  private final HTMLCanvasElement element;

  protected JSDrawing(JSDisplay display) {
    super(display);
    element = Globals.document.createElement(StringTypes.canvas);
    element.classList.add("merman-display-drawing");
  }

  @Override
  public void clear() {}

  @Override
  public void resize(Context context, Vector vector) {
    Display.UnconvertVector v =
        display.halfConvert.unconvertSpan(vector.converse, vector.transverse);
    element.style.width = v.x + "px";
    element.style.height = v.y + "px";
    fixPosition();
  }

  @Override
  public DrawingContext begin(Context context) {
    CanvasRenderingContext2D ctx = element.getContext(StringTypes._2d);
    return new DrawingContext() {
      private boolean stroke = true;

      @Override
      public void setLineColor(ModelColor color) {
        ctx.strokeStyle = union(JSDisplay.cssColor(color));
      }

      @Override
      public void setLineCapRound() {
        ctx.lineCap = "round";
      }

      @Override
      public void setLineThickness(double lineThickness) {
        ctx.lineWidth = lineThickness;
      }

      @Override
      public void setLineCapFlat() {
        ctx.lineCap = "butt";
      }

      @Override
      public void setFillColor(ModelColor color) {
        ctx.fillStyle = union(JSDisplay.cssColor(color));
      }

      @Override
      public void beginStrokePath() {
        stroke = true;
        ctx.beginPath();
      }

      @Override
      public void beginFillPath() {
        stroke = false;
        ctx.beginPath();
      }

      @Override
      public void moveTo(int c, int t) {
        if (stroke) {
          Display.UnconvertVector v =
              display.convert.unconvert(c, t, 1, 1, element.width, element.height);
          ctx.moveTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v =
              display.convert.unconvert(c, t, 0, 0, element.width, element.height);
          ctx.moveTo(v.x, v.y);
        }
      }

      @Override
      public void lineTo(int c, int t) {
        if (stroke) {
          Display.UnconvertVector v =
              display.convert.unconvert(c, t, 1, 1, element.width, element.height);
          ctx.lineTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v =
              display.convert.unconvert(c, t, 0, 0, element.width, element.height);
          ctx.lineTo(v.x, v.y);
        }
      }

      @Override
      public void closePath() {
        if (stroke) ctx.stroke();
        else ctx.fill();
      }

      @Override
      public void arcTo(int c, int t, int c2, int t2, int radius) {
        if (stroke) {
          Display.UnconvertVector v1 =
              display.convert.unconvert(c, t, 1, 1, element.width, element.height);
          Display.UnconvertVector v2 =
              display.convert.unconvert(c2, t2, 1, 1, element.width, element.height);
          ctx.arcTo(v1.x + 0.5, v1.y, v2.x + 0.5, v2.y + 0.5 + 0.5, radius);
        } else {
          Display.UnconvertVector v =
              display.convert.unconvert(c, t, 0, 0, element.width, element.height);
          Display.UnconvertVector v2 =
              display.convert.unconvert(c2, t2, 0, 0, element.width, element.height);
          ctx.arcTo(v.x, v.y, v2.x, v2.y, radius);
        }
      }

      @Override
      public void translate(int c, int t) {
        Display.UnconvertVector v =
            display.convert.unconvert(c, t, 0, 0, element.width, element.height);
        ctx.translate(v.x, v.y);
      }
    };
  }

  @Override
  public HTMLElement js() {
    return element;
  }
}
