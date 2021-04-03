package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.Display;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

public class JSBlank extends JSCourseDisplayNode implements Blank {
  private double converseSpan;

  protected JSBlank(JSDisplay display) {
    super(display, (HTMLElement) DomGlobal.document.createElement("div"));
    element.classList.add("merman-display-blank", "merman-display");
  }

  @Override
  public void setConverseSpan(Context context, double converse) {
    this.converseSpan = converse;
    Display.UnconvertAxis v = display.halfConvert.unconvertConverseSpan(converse);
    if (v.x) element.style.width = CSSProperties.WidthUnionType.of(v.amount + "px");
    else element.style.height = CSSProperties.HeightUnionType.of(v.amount + "px");
    fixPosition();
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  protected double converseCorner() {
    return converse;
  }

  @Override
  protected double transverseCorner() {
    return 0;
  }
}
