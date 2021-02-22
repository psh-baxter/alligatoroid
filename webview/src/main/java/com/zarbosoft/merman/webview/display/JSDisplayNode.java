package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
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

  public final HTMLElement js() {
    return element;
  }

  public void fixPosition() {
    fixPosition(false);
  }

  public void fixPosition(boolean animate) {
    Display.UnconvertVector v =
        display.convert.unconvert(
            converseCorner(), transverseCorner(), js().clientWidth, js().clientHeight);
    if (animate) {
      js().classList.add(CSS_ANIMATE_LEFT);
      js().classList.add(CSS_ANIMATE_TOP);
    } else {
      js().classList.remove(CSS_ANIMATE_LEFT);
      js().classList.remove(CSS_ANIMATE_TOP);
    }
    js().style.left = v.x + "px";
    js().style.top = v.y + "px";
  }

  protected abstract double transverseCorner();

  protected abstract double converseCorner();

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
}
