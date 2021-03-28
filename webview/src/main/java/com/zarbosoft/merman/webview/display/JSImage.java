package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.Image;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;

public class JSImage extends JSCourseDisplayNode implements Image {
  public JSImage(JSDisplay display) {
    super(display, (HTMLElement) DomGlobal.document.createElement("img"));
    element.classList.add("merman-display-img", "merman-display");
  }

  @Override
  public void setImage(Context context, String path) {
    ((HTMLImageElement)element).src = path;
    ascent = element.clientHeight;
    fixPosition();
  }

  @Override
  public void rotate(Context context, double rotate) {
    element.style.transform = "rotate(" + rotate + "deg)";
    fixPosition();
  }

  @Override
  public double converseSpan() {
    return element.clientWidth;
  }

  @Override
  protected double converseCorner() {
    return converse;
  }

  @Override
  protected double transverseCorner() {
    return transverseBaseline - ascent;
  }
}
