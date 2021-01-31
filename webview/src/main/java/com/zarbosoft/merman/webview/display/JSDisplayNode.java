package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import def.dom.HTMLElement;

public abstract class JSDisplayNode implements DisplayNode {
  public static final String CSS_ANIMATE_LEFT = "animate-left";
  public static final String CSS_ANIMATE_RIGHT = "animate-right";
  protected final JSDisplay display;
  private int converse;
  private int transverse;

  protected JSDisplayNode(JSDisplay display) {
    this.display = display;
  }

  public abstract HTMLElement js();

  @Override
  public int converse(Context context) {
    return converse;
  }

  @Override
  public int transverse(Context context) {
    return transverse;
  }

  @Override
  public int converseSpan(Context context) {
    return display.halfConvert.convert(js().clientWidth, js().clientHeight).converse;
  }

  @Override
  public int transverseSpan(Context context) {
    return display.halfConvert.convert(js().clientWidth, js().clientHeight).transverse;
  }

  @Override
  public void setConverse(Context context, int converse, boolean animate) {
    this.converse = converse;
    setPosition(
        display.convert.unconvertConverse(
            converse, js().clientWidth, js().clientHeight, display.width(), display.height()),
        animate);
  }

  @Override
  public void setTransverse(Context context, int transverse, boolean animate) {
    this.transverse = transverse;
    setPosition(
        display.convert.unconvertTransverse(
            transverse, js().clientWidth, js().clientHeight, display.width(), display.height()),
        animate);
  }

  public void fixPosition() {
    Display.UnconvertAxis v1 =
        display.convert.unconvertConverse(
            converse, js().clientWidth, js().clientHeight, display.width(), display.height());
    Display.UnconvertAxis v2 =
        display.convert.unconvertTransverse(
            transverse, js().clientWidth, js().clientHeight, display.width(), display.height());
    if (v1.x) {
      js().style.left = v1.amount + "px";
      js().style.top = v2.amount + "px";
    } else {
      js().style.top = v1.amount + "px";
      js().style.left = v2.amount + "px";
    }
  }

  private void setPosition(Display.UnconvertAxis v, boolean animate) {
    if (v.x) {
      if (animate) js().classList.add(CSS_ANIMATE_LEFT);
      else js().classList.remove(CSS_ANIMATE_LEFT);
      js().style.left = String.format("%spx", v.amount);
    } else {
      if (animate) js().classList.add(CSS_ANIMATE_RIGHT);
      else js().classList.remove(CSS_ANIMATE_RIGHT);
      js().style.top = String.format("%spx", v.amount);
    }
  }
}
