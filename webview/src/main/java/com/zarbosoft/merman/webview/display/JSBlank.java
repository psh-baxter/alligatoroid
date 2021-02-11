package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.Display;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class JSBlank extends JSDisplayNode implements Blank {
  private final HTMLDivElement element;

  protected JSBlank(JSDisplay display) {
    super(display);
    element = (HTMLDivElement) DomGlobal.document.createElement("div");
    element.classList.add("merman-display-blank");
  }

  @Override
  public HTMLElement js() {
    return element;
  }

  @Override
  public void setConverseSpan(Context context, int converse) {
    Display.UnconvertAxis v = display.halfConvert.unconvertConverseSpan(converse);
    if (v.x) element.style.width = CSSProperties.WidthUnionType.of(v.amount + "px");
    else element.style.height = CSSProperties.HeightUnionType.of(v.amount + "px");
    fixPosition();
  }

  @Override
  public void setTransverseSpan(Context context, int transverse) {
    Display.UnconvertAxis v = display.halfConvert.unconvertTransverseSpan(transverse);
    if (v.x) element.style.width = CSSProperties.WidthUnionType.of(v.amount + "px");
    else element.style.height = CSSProperties.HeightUnionType.of(v.amount + "px");
    fixPosition();
  }
}
