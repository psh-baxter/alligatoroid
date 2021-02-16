package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.rendaw.common.ROList;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Node;

public class JSGroup extends JSDisplayNode implements Group {
  private final HTMLDivElement element;

  protected JSGroup(JSDisplay display) {
    super(display);
    element = (HTMLDivElement) DomGlobal.document.createElement("div");
    element.classList.add("merman-display-group", "merman-display");
  }

  @Override
  public void add(int index, DisplayNode node) {
    if (index < element.childNodes.length) element.insertBefore(((JSDisplayNode) node).js(), element.childNodes.getAt(index));
    else element.appendChild(((JSDisplayNode) node).js());
    fixPosition();
  }

  @Override
  public void addAll(int index, ROList<? extends DisplayNode> nodes) {
    if (index < element.childNodes.length) {
      Node following = element.childNodes.getAt(index);
      for (DisplayNode node : nodes) {
        element.insertBefore(((JSDisplayNode) node).js(), following);
      }
    }
    else {
      for (DisplayNode node : nodes) {
        element.appendChild(((JSDisplayNode) node).js());
      }
    }
    fixPosition();
  }

  @Override
  public void remove(int start, int count) {
    for (int i = 0; i < count; ++i) {
      ((HTMLElement) element.childNodes.getAt(start)).remove();
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
