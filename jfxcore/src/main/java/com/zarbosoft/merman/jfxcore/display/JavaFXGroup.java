package com.zarbosoft.merman.jfxcore.display;

import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.rendaw.common.ROList;
import javafx.geometry.Bounds;

import java.util.List;

public class JavaFXGroup extends JavaFXFreeDisplayNode implements Group {
  protected JavaFXGroup(JavaFXDisplay display) {
    super(display, new javafx.scene.Group());
  }

  @Override
  public void add(final int index, final DisplayNode node) {
    ((javafx.scene.Group) this.node).getChildren().add(index, ((JavaFXNode) node).node());
  }

  @Override
  public void addAll(final int index, final ROList<? extends DisplayNode> nodes) {
    ((javafx.scene.Group) this.node).getChildren().addAll(index, (List) nodes.inner_());
  }

  @Override
  public void remove(final int index, final int count) {
    ((javafx.scene.Group) this.node).getChildren().subList(index, index + count).clear();
  }

  @Override
  public void remove(final DisplayNode node) {
    ((javafx.scene.Group) this.node).getChildren().remove(((JavaFXNode) node).node());
  }

  @Override
  public int size() {
    return ((javafx.scene.Group) this.node).getChildren().size();
  }

  @Override
  public void clear() {
    ((javafx.scene.Group) this.node).getChildren().clear();
  }

  @Override
  public void setTransverse(double transverse, boolean animate) {
    this.transverse = transverse;
    fixPosition(animate);
  }

  @Override
  public double transverseSpan() {
    Bounds bounds = this.node.getLayoutBounds();
    return this.display.convert.convert(
            bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY())
        .transverse;
  }
}
