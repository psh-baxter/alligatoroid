package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.Display;
import def.dom.Globals;
import def.dom.HTMLDivElement;
import def.dom.HTMLElement;
import jsweet.util.StringTypes;

public class JSBlank extends JSDisplayNode implements Blank {
  private final HTMLDivElement element;

  protected JSBlank(JSDisplay display) {
    super(display);
    element = Globals.document.createElement(StringTypes.div);
    element.classList.add("merman-display-blank");
  }

  @Override
  public HTMLElement js() {
    return element;
  }

  @Override
  public void setConverseSpan(Context context, int converse) {
    Display.UnconvertAxis v = display.halfConvert.unconvertConverseSpan(converse);
    if (v.x) element.style.width = v.amount + "px";
    else element.style.height = v.amount + "px";
    fixPosition();
  }

  @Override
  public void setTransverseSpan(Context context, int transverse) {
    Display.UnconvertAxis v = display.halfConvert.unconvertTransverseSpan(transverse);
    if (v.x) element.style.width = v.amount + "px";
    else element.style.height = v.amount + "px";
    fixPosition();
  }
}
