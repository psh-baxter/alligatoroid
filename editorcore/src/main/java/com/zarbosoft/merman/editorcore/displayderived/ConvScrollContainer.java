package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.visual.Vector;

public class ConvScrollContainer implements Container {
  public FreeDisplayNode inner;
  private double converseSpan;

  public ConvScrollContainer() {
  }

  public void scroll(double abs, boolean animate) {
    inner.setConverse(abs, animate);
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
    inner.setConverse(converse, animate);
  }

  @Override
  public Object inner_() {
    return inner.inner_();
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    inner.setPosition(vector, animate);
  }

  @Override
  public void setConverseSpan(Context context, double span) {
    this.converseSpan = span;
  }
}
