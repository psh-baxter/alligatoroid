package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.visual.Vector;

public class ConvScrollContainer implements Container {
  public FreeDisplayNode inner;
  private double converseSpan;
  private double baseConverse;
  private double childMin;
  private double childMax;
  private boolean animate;
  private double scrollAbs;

  public ConvScrollContainer() {}

  public void scrollVisible(double childMin, double childMax, boolean animate) {
    this.childMin = childMin;
    this.childMax = childMax;
    this.animate = animate;
    updateScroll();
  }

  private void updateScroll() {
    if (converseSpan == 0) return;
    double newScroll = scrollAbs;
    double overMax = childMax + newScroll - converseSpan;
    if (overMax > 0) {
      newScroll -= overMax;
    }
    double underMin = childMin + newScroll;
    if (underMin < 0) {
      newScroll -= underMin;
    }
    if (newScroll == scrollAbs) return;
    scrollAbs = newScroll;
    inner.setConverse(baseConverse + scrollAbs, animate);
  }

  @Override
  public double converse() {
    return inner.converse();
  }

  @Override
  public double transverse() {
    return inner.transverse();
  }

  @Override
  public double transverseSpan() {
    return inner.transverseSpan();
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    baseConverse = converse;
    inner.setConverse(baseConverse + scrollAbs, animate);
  }

  @Override
  public Object inner_() {
    return inner.inner_();
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    baseConverse = vector.converse;
    inner.setPosition(new Vector(baseConverse + scrollAbs, vector.transverse), animate);
  }

  @Override
  public void setConverseSpan(Context context, double span) {
    this.converseSpan = span;
    updateScroll();
  }
}
