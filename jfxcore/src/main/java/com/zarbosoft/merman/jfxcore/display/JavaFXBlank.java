package com.zarbosoft.merman.jfxcore.display;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.visual.Vector;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class JavaFXBlank implements Blank, JavaFXNode {
  private final Region node;
  private double converseSpan;
  private double converse;
  private double transverse;

  protected JavaFXBlank() {
    this.node = new Region();
    this.node.setMouseTransparent(true);
  }

  @Override
  public Node node() {
    return node;
  }

  @Override
  public void setConverseSpan(Context context, double converse) {
    this.converseSpan = converse;
  }

  @Override
  public double converse() {
    return converse;
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    this.converse = converse;
  }

  @Override
  public void setBaselineTransverse(double transverse, boolean animate) {
    this.transverse = transverse;
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    this.converse = vector.converse;
    this.transverse = vector.transverse;
  }

  @Override
  public double baselineTransverse() {
    return this.transverse;
  }

  @Override
  public double ascent() {
    return 0;
  }

  @Override
  public double descent() {
    return 0;
  }
}
