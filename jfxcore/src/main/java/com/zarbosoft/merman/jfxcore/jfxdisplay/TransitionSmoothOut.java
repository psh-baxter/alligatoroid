package com.zarbosoft.merman.jfxcore.jfxdisplay;

import javafx.animation.Transition;
import javafx.scene.Node;

public class TransitionSmoothOut extends Transition {
  private final Node node;
  private final Double diffX;
  private final Double diffY;

  {
    setCycleDuration(javafx.util.Duration.millis(200));
  }

  TransitionSmoothOut(final Node node, final Double diffX, final Double diffY) {
    this.node = node;
    this.diffX = diffX;
    this.diffY = diffY;
  }

  @Override
  protected void interpolate(final double frac) {
    final double frac2 = Math.pow(1 - frac, 3);
    if (diffX != null) node.setTranslateX(-frac2 * diffX);
    if (diffY != null) node.setTranslateY(-frac2 * diffY);
  }
}
