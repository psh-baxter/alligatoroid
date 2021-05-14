package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.rendaw.common.Format;
import elemental2.dom.HTMLElement;

public abstract class JSDisplayNode implements DisplayNode {
  public static final String CSS_ANIMATE_LEFT = "animate-left";
  public static final String CSS_ANIMATE_TOP = "animate-top";
  protected final JSDisplay display;
  public HTMLElement element;

  protected JSDisplayNode(JSDisplay display, HTMLElement element) {
    this.display = display;
    this.element = element;
  }

  @Override
  public final HTMLElement inner_() {
    return element;
  }

  public void fixPosition() {
    fixPosition(false);
  }

  public void fixPosition(boolean animate) {
    Display.UnconvertVector v =
        display.convert.unconvert(
            converseCorner(), transverseCorner(), inner_().clientWidth, inner_().clientHeight);
    if (animate) {
      inner_().classList.add(CSS_ANIMATE_LEFT);
      inner_().classList.add(CSS_ANIMATE_TOP);
    } else {
      inner_().classList.remove(CSS_ANIMATE_LEFT);
      inner_().classList.remove(CSS_ANIMATE_TOP);
    }
    inner_().style.left = v.x + "px";
    inner_().style.top = v.y + "px";
  }

  protected abstract double transverseCorner();

  protected abstract double converseCorner();

  protected void setJSPositionInternal(Display.UnconvertAxis v, boolean animate) {
    if (v.x) {
      if (animate) inner_().classList.add(CSS_ANIMATE_LEFT);
      else inner_().classList.remove(CSS_ANIMATE_LEFT);
      inner_().style.left = Format.format("%spx", v.amount);
    } else {
      if (animate) inner_().classList.add(CSS_ANIMATE_TOP);
      else inner_().classList.remove(CSS_ANIMATE_TOP);
      inner_().style.top = Format.format("%spx", v.amount);
    }
  }
}
