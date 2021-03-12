package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.DrawingContext;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.Format;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CSSProperties;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLElement;

public class JSDrawing extends JSFreeDisplayNode implements Drawing {
  private Vector size;

  protected JSDrawing(JSDisplay display) {
    super(display, (HTMLElement) DomGlobal.document.createElement("canvas"));
    element.classList.add("merman-display-drawing", "merman-display");
  }

  @Override
  public void clear() {
    HTMLCanvasElement element = (HTMLCanvasElement) this.element;
    CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) element.getContext("2d");
    ctx.clearRect(0, 0, element.width, element.height);
  }

  @Override
  public void resize(Context context, Vector vector) {
    HTMLCanvasElement element = (HTMLCanvasElement) this.element;
    this.size = vector;
    Display.UnconvertVector v =
        display.halfConvert.unconvertSpan(vector.converse, vector.transverse);

    int width = (int) v.x + 1;
    int height = (int) v.y + 1;

    CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) element.getContext("2d");
    double pixelRatio = JSDisplay.canvasPixelRatio(ctx);
    element.width = (int) (width * pixelRatio);
    element.height = (int) (height * pixelRatio);
    element.style.width = CSSProperties.WidthUnionType.of(Format.format("%spx", width));
    element.style.height = CSSProperties.HeightUnionType.of(Format.format("%spx", height));
    ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    fixPosition();
  }

  @Override
  public DrawingContext begin(Context context) {
    HTMLCanvasElement element = (HTMLCanvasElement) this.element;
    CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) element.getContext("2d");
    return new DrawingContext() {
      private boolean stroke = true;

      @Override
      public void setLineColor(ModelColor color) {
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(JSDisplay.cssColor(color));
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
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(JSDisplay.cssColor(color));
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
      public void moveTo(double c, double t) {
        if (stroke) {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 1, 1);
          ctx.moveTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          ctx.moveTo(v.x, v.y);
        }
      }

      @Override
      public void lineTo(double c, double t) {
        if (stroke) {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 1, 1);
          ctx.lineTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          ctx.lineTo(v.x, v.y);
        }
      }

      @Override
      public void closePath() {
        if (stroke) ctx.stroke();
        else ctx.fill();
      }

      @Override
      public void arcTo(double c, double t, double c2, double t2, double radius) {
        if (stroke) {
          Display.UnconvertVector v1 = display.convert.unconvert(c, t, 1, 1);
          Display.UnconvertVector v2 = display.convert.unconvert(c2, t2, 1, 1);
          ctx.arcTo(v1.x + 0.5, v1.y, v2.x + 0.5, v2.y + 0.5 + 0.5, radius);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          Display.UnconvertVector v2 = display.convert.unconvert(c2, t2, 0, 0);
          ctx.arcTo(v.x, v.y, v2.x, v2.y, radius);
        }
      }

      @Override
      public void translate(double c, double t) {
        Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
        ctx.translate(v.x, v.y);
      }
    };
  }

  @Override
  public double transverseSpan() {
    return size.transverse;
  }

  @Override
  public double converseSpan() {
    return size.converse;
  }
}
