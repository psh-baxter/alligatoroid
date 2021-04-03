package com.zarbosoft.merman.jfxcore.jfxdisplay;

import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.visual.Vector;
import javafx.scene.Node;

public abstract class JavaFXFreeDisplayNode extends JavaFXCommonBaseNode implements FreeDisplayNode {
  double converse;
  double transverse;

  protected JavaFXFreeDisplayNode(JavaFXDisplay display, Node element) {
    super(display, element);
  }

  @Override
  public double converse() {
    return converse;
  }

  @Override
  public double transverse() {
    return transverse;
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    this.converse = converse;
    fixPosition(animate);
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    this.converse = vector.converse;
    this.transverse = vector.transverse;
    fixPosition(animate);
  }

  @Override
  protected double transverseCorner() {
    return transverse;
  }

  @Override
  protected double converseCorner() {
    return converse;
  }
}
