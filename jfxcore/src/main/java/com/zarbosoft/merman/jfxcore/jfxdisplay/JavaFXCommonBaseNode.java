package com.zarbosoft.merman.jfxcore.jfxdisplay;

import com.zarbosoft.merman.core.editor.display.Display;
import com.zarbosoft.merman.core.editor.display.DisplayNode;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public abstract class JavaFXCommonBaseNode implements DisplayNode, JavaFXNode {
  public final Node node;
  protected final JavaFXDisplay display;

  @Override
  public Node node() {
    return node;
  }

  protected JavaFXCommonBaseNode(JavaFXDisplay display, Node node) {
    this.display = display;
    this.node = node;
  }

  @Override
  public double converseSpan() {
    Bounds layoutBounds = node.getLayoutBounds();
    return display.halfConvert.convert(layoutBounds.getWidth(), layoutBounds.getHeight()).converse;
  }

  protected abstract double transverseCorner();

  protected abstract double converseCorner();

  public void fixPosition() {
    fixPosition(false);
  }

  public void fixPosition(boolean animate) {
    Bounds bounds = node.getLayoutBounds();
    Display.UnconvertVector v =
        display.convert.unconvert(
            converseCorner(), transverseCorner(), bounds.getWidth(), bounds.getHeight());
    if (animate)
      new TransitionSmoothOut(node, v.x - node.getLayoutX(), v.y - node.getLayoutY())
          .play();
    node.setLayoutX(v.x);
    node.setLayoutY(v.y);
  }

  public void setJFXPositionInternal(final Display.UnconvertAxis v, final boolean animate) {
    if (v.x) {
      if (animate) new TransitionSmoothOut(node, v.amount - node.getLayoutX(), 0.0).play();
      node.setLayoutX(v.amount);
    } else {
      if (animate) new TransitionSmoothOut(node, 0.0, v.amount - node.getLayoutY()).play();
      node.setLayoutY(v.amount);
    }
  }
}
