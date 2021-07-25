package com.zarbosoft.merman.jfxcore.display;

import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public abstract class JavaFXCommonBaseNode implements DisplayNode {
  public final Node node;
  protected final JavaFXDisplay display;
  private Transition transition;

  protected JavaFXCommonBaseNode(JavaFXDisplay display, Node node) {
    this.display = display;
    this.node = node;
    this.node.setMouseTransparent(true);
  }

  @Override
  public final Node inner_() {
    return node;
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
      transition(
          new TransitionSmoothOut(
              node,
              v.x - (node.getLayoutX() + node.getTranslateX()),
              v.y - (node.getLayoutY() + node.getTranslateY())));
    node.setLayoutX(v.x);
    node.setLayoutY(v.y);
  }

  private void transition(Transition newTransition) {
    if (transition != null) transition.stop();
    transition = newTransition;
    transition.setOnFinished(
        e -> {
          transition = null;
        });
    transition.play();
  }

  public void setJFXPositionInternal(final Display.UnconvertAxis v, final boolean animate) {
    if (v.x) {
      if (animate)
        transition(
            new TransitionSmoothOut(
                node, v.amount - (node.getLayoutX() + node.getTranslateX()), null));
      node.setLayoutX(v.amount);
    } else {
      if (animate)
        transition(
            new TransitionSmoothOut(
                node, null, v.amount - (node.getLayoutY() + node.getTranslateY())));
      node.setLayoutY(v.amount);
    }
  }
}
