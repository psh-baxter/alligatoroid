package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.rendaw.common.Format;
import elemental2.dom.HTMLElement;

public abstract class JSDisplayNode implements DisplayNode {
  public static final String CSS_ANIMATE_LEFT = "animate-left";
  public static final String CSS_ANIMATE_TOP = "animate-top";
  protected final JSDisplay display;
  public HTMLElement element;
  protected int converse;
  protected int transverse;

  protected JSDisplayNode(JSDisplay display, HTMLElement element) {
    this.display = display;
    this.element = element;
    element.style.pointerEvents = "none";
  }

  public final HTMLElement js() {
    return element;
  }

  @Override
  public int converse() {
    return converse;
  }

  @Override
  public final int converseSpan() {
    return display.halfConvert.convert(js().clientWidth, js().clientHeight).converse;
  }

  @Override
  public final void setConverse(int converse, boolean animate) {
    this.converse = converse;
    setJSPositionInternal(
        display.convert.unconvertConverse(
            converse, js().clientWidth, js().clientHeight, display.width(), display.height()),
        animate);
  }

  public final void setTransverse(int transverse, boolean animate) {
    this.transverse = transverse;
    setJSPositionInternal(
        display.convert.unconvertTransverse(
            transverse, js().clientWidth, js().clientHeight, display.width(), display.height()),
        animate);
  }

  public void fixPosition() {
    Display.UnconvertVector v =
        display.convert.unconvert(
            converse,
            transverse,
            js().clientWidth,
            js().clientHeight,
            display.width(),
            display.height());
    js().style.left = v.x + "px";
    js().style.top = v.y + "px";
  }

  public void setPosition(Vector vector, boolean animate) {
    Display.UnconvertVector vector1 =
        display.convert.unconvert(
            vector.converse,
            vector.transverse,
            js().clientWidth,
            js().clientHeight,
            display.width(),
            display.height());
    if (animate) {
      js().classList.add(CSS_ANIMATE_LEFT);
      js().classList.add(CSS_ANIMATE_TOP);
    }
    this.converse = vector.converse;
    this.transverse = vector.transverse;
    js().style.left = Format.format("%spx", vector1.x);
    js().style.top = Format.format("%spx", vector1.y);
  }

  protected void setJSPositionInternal(Display.UnconvertAxis v, boolean animate) {
    if (v.x) {
      if (animate) js().classList.add(CSS_ANIMATE_LEFT);
      else js().classList.remove(CSS_ANIMATE_LEFT);
      js().style.left = Format.format("%spx", v.amount);
    } else {
      if (animate) js().classList.add(CSS_ANIMATE_TOP);
      else js().classList.remove(CSS_ANIMATE_TOP);
      js().style.top = Format.format("%spx", v.amount);
    }
  }

  @Override
  public int transverse() {
    return transverse;
  }
}
