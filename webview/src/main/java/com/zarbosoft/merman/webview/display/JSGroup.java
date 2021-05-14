package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.rendaw.common.ROList;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.Node;

public class JSGroup extends JSFreeDisplayNode implements Group {
  protected JSGroup(JSDisplay display) {
    super(display, (HTMLElement) DomGlobal.document.createElement("div"));
    element.classList.add("merman-display-group", "merman-display");
  }

  @Override
  public void add(int index, DisplayNode node) {
    if (index < element.childNodes.length)
      element.insertBefore(((JSDisplayNode) node).inner_(), element.childNodes.getAt(index));
    else element.appendChild(((JSDisplayNode) node).inner_());
    fixPosition();
  }

  @Override
  public void setTransverse(double transverse, boolean animate) {
    this.transverse = transverse;
    fixPosition(animate);
  }

  @Override
  public void addAll(int index, ROList<? extends DisplayNode> nodes) {
    if (index < element.childNodes.length) {
      Node following = element.childNodes.getAt(index);
      for (DisplayNode node : nodes) {
        element.insertBefore(((JSDisplayNode) node).inner_(), following);
      }
    } else {
      for (DisplayNode node : nodes) {
        element.appendChild(((JSDisplayNode) node).inner_());
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
    ((JSDisplayNode) node).inner_().remove();
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
  public double transverseSpan() {
    return  this.element.getBoundingClientRect().height;
  }

  @Override
  public double converseSpan() {
    return  this.element.getBoundingClientRect().width;
  }
}
