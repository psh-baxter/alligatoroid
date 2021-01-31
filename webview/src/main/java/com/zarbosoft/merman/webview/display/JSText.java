package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.syntax.style.ModelColor;
import def.dom.Globals;
import def.dom.HTMLDivElement;
import def.dom.HTMLElement;
import jsweet.util.StringTypes;

public class JSText extends JSDisplayNode implements Text {
  private JSFont font;
  private final HTMLDivElement element;

  protected JSText(JSDisplay display) {
    super(display);
    element = Globals.document.createElement(StringTypes.div);
    element.classList.add("merman-display-text");
  }

  @Override
  public String text() {
    return element.textContent;
  }

  @Override
  public void setText(Context context, String text) {
    element.textContent = text;
    fixPosition();
  }

  @Override
  public void setColor(Context context, ModelColor color) {
    element.style.color = JSDisplay.cssColor(color);
  }

  @Override
  public Font font() {
    return font;
  }

  @Override
  public void setFont(Context context, Font font) {
    this.font = (JSFont) font;
    element.style.fontFamily = this.font.name;
    element.style.fontSize = this.font.size + "pt";
    fixPosition();
  }

  @Override
  public int getIndexAtConverse(Context context, int converse) {
    for (int i = 0; i < element.textContent.length(); ++i) {}

    return 0;
  }

  @Override
  public int getConverseAtIndex(int index) {
    return (int) font.measure(text()).width;
  }

  @Override
  public HTMLElement js() {
    return element;
  }
}
