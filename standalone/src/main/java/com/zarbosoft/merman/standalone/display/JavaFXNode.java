package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public abstract class JavaFXNode implements DisplayNode {
  protected final JavaFXDisplay display;
  private int converse = 0;
  private int transverse = 0;

  protected JavaFXNode(JavaFXDisplay display) {
    this.display = display;
  }

  protected abstract Node node();

  @Override
  public final double converseSpan() {
    Bounds layoutBounds = node().getLayoutBounds();
    return display.halfConvert.convert(layoutBounds.getWidth(), layoutBounds.getHeight()).converse;
  }

  @Override
  public final double converse() {
    return converse;
  }

  @Override
  public final double baselineTransverse() {
    return transverse;
  }

  public static class TransitionSmoothOut extends Transition {
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

  @Override
  public final void setBaselineTransverse(final double transverse, final boolean animate) {
    Bounds layoutBounds = node().getLayoutBounds();
    this.transverse = transverse;
    setPosition(
        display.convert.unconvertTransverse(
            transverse,
            layoutBounds.getWidth(),
            layoutBounds.getHeight()),
        animate);
  }

  @Override
  public final void setConverse(final double converse, final boolean animate) {
    Bounds layoutBounds = node().getLayoutBounds();
    this.transverse = transverse;
    this.converse = converse;
    setPosition(
        display.convert.unconvertConverse(
            converse,
            layoutBounds.getWidth(),
            layoutBounds.getHeight()
            ),
        animate);
  }

  public void fixPosition() {
    Bounds layoutBounds = node().getLayoutBounds();
    Display.UnconvertVector v =
        display.convert.unconvert(
            converse,
            transverse,
            layoutBounds.getWidth(),
            layoutBounds.getHeight()
            );
    node().setLayoutX(v.x);
    node().setLayoutY(v.y);
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    Bounds layoutBounds = node().getLayoutBounds();
    Display.UnconvertVector vector1 = display.convert.unconvert(vector.converse, vector.transverse,
            layoutBounds.getWidth(),
            layoutBounds.getHeight()
    );
    if (animate) new TransitionSmoothOut(node(), vector1.x - node().getLayoutX(), vector1.y - node().getLayoutY()).play();
    node().setLayoutX(vector1.x);
    node().setLayoutY(vector1.y);
  }

  public void setPosition(final Display.UnconvertAxis v, final boolean animate) {
    if (v.x) {
      if (animate) new TransitionSmoothOut(node(), v.amount - node().getLayoutX(), 0.0).play();
      node().setLayoutX(v.amount);
    } else {
      if (animate) new TransitionSmoothOut(node(), 0.0, v.amount - node().getLayoutY()).play();
      node().setLayoutY(v.amount);
    }
  }
}
