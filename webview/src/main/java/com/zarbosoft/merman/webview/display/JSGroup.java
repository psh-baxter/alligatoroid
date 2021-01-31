package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.rendaw.common.ROList;
import def.dom.Globals;
import def.dom.HTMLDivElement;
import def.dom.HTMLElement;
import jsweet.util.StringTypes;

public class JSGroup extends JSDisplayNode implements Group {
  private final HTMLDivElement element;

  protected JSGroup(JSDisplay display) {
    super(display);
    element = Globals.document.createElement(StringTypes.div);
    element.classList.add("merman-display-group");
  }

  @Override
  public void add(int index, DisplayNode node) {
    if (index < element.childNodes.length) element.appendChild(((JSDisplayNode) node).js());
    else element.childNodes.$get(index + 1).insertBefore(((JSDisplayNode) node).js());
    fixPosition();
  }

  @Override
  public void addAll(int index, ROList<? extends DisplayNode> nodes) {
    for (int i = 0; i < nodes.size(); ++i) {
      DisplayNode node = nodes.get(i);
      if (index < element.childNodes.length) element.appendChild(((JSDisplayNode) node).js());
      else element.childNodes.$get(index + 1).insertBefore(((JSDisplayNode) node).js());
      index += 1;
    }
    fixPosition();
  }

  @Override
  public void remove(int index, int count) {
    for (int i = 0; i < count; ++i) {
      ((HTMLElement) element.childNodes.$get(i)).remove();
    }
    fixPosition();
  }

  @Override
  public void remove(DisplayNode node) {
    ((JSDisplayNode) node).js().remove();
    fixPosition();
  }

  @Override
  public int size() {
    return element.childNodes.length;
  }

  @Override
  public void clear() {
    element.innerHTML = "";
    fixPosition();
  }

  @Override
  public HTMLElement js() {
    return element;
  }
}
