package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Image;
import def.dom.Globals;
import def.dom.HTMLElement;
import def.dom.HTMLImageElement;
import jsweet.util.StringTypes;

public class JSImage extends JSDisplayNode implements Image {
  private final HTMLImageElement element;

  public JSImage(JSDisplay display) {
    super(display);
    element = Globals.document.createElement(StringTypes.img);
    element.classList.add("merman-display-img");
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
