package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Image;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;

public class JSImage extends JSDisplayNode implements Image {
  private final HTMLImageElement element;

  public JSImage(JSDisplay display) {
    super(display);
    element = (HTMLImageElement) DomGlobal.document.createElement("img");
    element.classList.add("merman-display-img", "merman-display");
  }

  @Override
  public HTMLElement js() {
    return element;
  }

  @Override
  public void setImage(Context context, String path) {
    element.src = path;
    fixPosition();
  }

  @Override
  public void rotate(Context context, double rotate) {
    element.style.transform = "rotate(" + rotate + "deg)";
    fixPosition();
  }
}
