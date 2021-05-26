package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.visual.Vector;

public class PadContainer extends StackContainer {
  private final double padConverseStart;
  private final double padConverseEnd;
  private final double padTransverseStart;
  private final double padTransverseEnd;

  public PadContainer(Context context, Padding padding) {
    super(context);
    this.padConverseStart = padding.converseStart * context.toPixels;
    this.padConverseEnd = padding.converseEnd * context.toPixels;
    this.padTransverseStart = padding.transverseStart * context.toPixels;
    this.padTransverseEnd = padding.transverseEnd * context.toPixels;
  }

  @Override
  public StackContainer addRoot(Container node) {
    super.addRoot(node);
    setPosition(Vector.zero, false);
    return this;
  }

  public void setConverseSpan(Context context, double span) {
    root.setConverseSpan(context, span - padConverseStart - padConverseEnd);
  }

  @Override
  public double converse() {
    return root.converse() - padConverseStart;
  }

  @Override
  public double transverse() {
    return root.transverse() - padTransverseStart;
  }

  @Override
  public double transverseSpan() {
    return padTransverseStart + root.transverseSpan() + padTransverseEnd;
  }

  @Override
  public double converseSpan() {
    return padConverseStart + root.converseSpan() + padConverseEnd;
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    root.setConverse(padConverseStart + converse, animate);
  }

  @Override
  public Object inner_() {
    return root.inner_();
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    root.setPosition(
        new Vector(vector.converse + padConverseStart, vector.transverse + padTransverseStart),
        animate);
  }
}
