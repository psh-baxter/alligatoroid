package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.webview.compat.TextMetrics;
import com.zarbosoft.rendaw.common.Assertion;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLElement;

public class JSText extends JSCourseDisplayNode implements Text {
  private JSFont font;
  private String text;
  private String color = "black";
  private double padConverseHalf;
  private double padTransverseHalf;
  private double converseSpan;

  protected JSText(JSDisplay display) {
    super(display, (HTMLElement) DomGlobal.document.createElement("canvas"));
    element.classList.add("merman-display-text", "merman-display");
  }

  @Override
  public String text() {
    return text;
  }

  @Override
  public void setText(Context context, String text) {
    this.text = text;
    render();
  }

  private void render() {
    if (this.font == null) return;
    if (this.text == null) return;
    if (this.color == null) throw new Assertion();
    HTMLCanvasElement element = (HTMLCanvasElement) this.element;
    CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) element.getContext("2d");

    ctx.setFont(this.font.cssString());
    TextMetrics out = (TextMetrics) ctx.measureText(text);

    converseSpan = out.width;
    {
      double padConverse = converseSpan / 2;
      padConverseHalf = padConverse / 2;
    }

    ascent = out.actualBoundingBoxAscent;
    ascent = ascent < 0 ? 0 : ascent;
    descent = out.actualBoundingBoxDescent;
    descent = descent < 0 ? 0 : descent;
    {
      double transverseCore = ascent + descent;
      double padTransverse = transverseCore / 2;
      padTransverseHalf = padTransverse / 2;
    }

    element.width = (int) Math.ceil(converseSpan + padConverseHalf * 2);
    element.height = (int) Math.ceil(ascent + descent + padTransverseHalf * 2);

    ctx.font = this.font.cssString();
    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(this.color);
    ctx.fillText(text, padConverseHalf, padTransverseHalf + ascent);

    fixPosition();
  }

  @Override
  public void setColor(Context context, ModelColor color) {
    this.color = JSDisplay.cssColor(color);
    render();
  }

  @Override
  public Font font() {
    return font;
  }

  @Override
  public void setFont(Context context, Font font) {
    this.font = (JSFont) font;
    render();
  }

  @Override
  public int getIndexAtConverse(Context context, double converse) {
    for (int i = 0; i < element.textContent.length(); ++i) {}

    return 0;
  }

  @Override
  public double getConverseAtIndex(int index) {
    return (int) font.measure(text()).width;
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  protected double converseCorner() {
    return converse - padConverseHalf;
  }

  @Override
  protected double transverseCorner() {
    return transverseBaseline - ascent - padTransverseHalf;
  }
}
