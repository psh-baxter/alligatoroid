package com.zarbosoft.merman.jfxcore.jfxdisplay;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.Drawing;
import com.zarbosoft.merman.core.display.DrawingContext;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;

public class JavaFXDrawing extends JavaFXFreeDisplayNode implements Drawing {
  private Vector size;

  protected JavaFXDrawing(JavaFXDisplay display) {
    super(display, new Canvas());
  }

  @Override
  public void clear() {
    final GraphicsContext gc = ((Canvas) node).getGraphicsContext2D();
    gc.setTransform(new Affine());
    gc.clearRect(0, 0, ((Canvas) node).getWidth(), ((Canvas) node).getHeight());
  }

  @Override
  public void resize(final Context context, final Vector vector) {
    this.size = vector;
    Display.UnconvertVector v =
        display.halfConvert.unconvertSpan(vector.converse, vector.transverse);
    ((Canvas) node).setWidth(Math.max(v.x, 1));
    ((Canvas) node).setHeight(Math.max(v.y, 1));
  }

  @Override
  public DrawingContext begin(final Context context) {
    final GraphicsContext gc = ((Canvas) node).getGraphicsContext2D();
    return new DrawingContext() {
      Boolean stroke = null;

      @Override
      public void setLineColor(final ModelColor color) {
        gc.setStroke(Helper.convert(color));
      }

      @Override
      public void setLineCapRound() {
        gc.setLineCap(StrokeLineCap.ROUND);
      }

      @Override
      public void setLineThickness(final double lineThickness) {
        gc.setLineWidth(lineThickness);
      }

      @Override
      public void setLineCapFlat() {
        gc.setLineCap(StrokeLineCap.BUTT);
      }

      @Override
      public void setFillColor(final ModelColor color) {
        gc.setFill(Helper.convert(color));
      }

      @Override
      public void moveTo(final double c, final double t) {
        if (stroke) {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 1, 1);
          gc.moveTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          gc.moveTo(v.x, v.y);
        }
      }

      @Override
      public void lineTo(final double c, final double t) {
        if (stroke) {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 1, 1);
          gc.lineTo(v.x + 0.5, v.y + 0.5);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          gc.lineTo(v.x, v.y);
        }
      }

      @Override
      public void beginStrokePath() {
        stroke = true;
        gc.beginPath();
      }

      @Override
      public void beginFillPath() {
        stroke = false;
        gc.beginPath();
      }

      @Override
      public void closePath() {
        gc.closePath();
        if (stroke == null) throw new AssertionError();
        if (stroke) gc.stroke();
        else gc.fill();
        stroke = null;
      }

      @Override
      public void arcTo(double c, double t, double c2, double t2, double radius) {
        if (stroke) {
          Display.UnconvertVector v1 = display.convert.unconvert(c, t, 1, 1);
          Display.UnconvertVector v2 = display.convert.unconvert(c2, t2, 1, 1);
          gc.arcTo(v1.x + 0.5, v1.y, v2.x + 0.5, v2.y + 0.5 + 0.5, radius);
        } else {
          Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
          Display.UnconvertVector v2 = display.convert.unconvert(c2, t2, 0, 0);
          gc.arcTo(v.x, v.y, v2.x, v2.y, radius);
        }
      }

      @Override
      public void translate(double c, double t) {
        Display.UnconvertVector v = display.convert.unconvert(c, t, 0, 0);
        gc.translate(v.x, v.y);
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
