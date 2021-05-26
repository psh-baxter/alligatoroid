package com.zarbosoft.merman.core.display.derived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.visual.Vector;

/** A group that maintains course display node childrens' baselines. */
public class CourseGroup implements CourseDisplayNode {
  private final Group group;
  private double baselineTransverse;
  private double ascent;
  private double descent;
  private double converseSpan;
  private double padConverse;
  private double padConverseEnd;
  private double padTransverse;
  private double padTransverseEnd;

  public CourseGroup(Group group) {
    this.group = group;
  }

  public void setPadding(Context context, Padding padding) {
    padConverse = padding.converseStart * context.toPixels;
    padConverseEnd = padding.converseEnd * context.toPixels;
    padTransverse = padding.transverseStart * context.toPixels;
    padTransverseEnd = padding.transverseEnd * context.toPixels;
  }

  @Override
  public void setBaselineTransverse(double transverse, boolean animate) {
    baselineTransverse = transverse;
    updateLocation(animate);
  }

  private void updateLocation(boolean animate) {
    group.setTransverse(baselineTransverse, animate);
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    baselineTransverse = vector.transverse;
    group.setPosition(new Vector(vector.converse + padConverse, baselineTransverse), animate);
  }

  @Override
  public double baselineTransverse() {
    return baselineTransverse;
  }

  @Override
  public double ascent() {
    return ascent + padTransverse;
  }

  @Override
  public double descent() {
    return descent + padTransverseEnd;
  }

  @Override
  public double converse() {
    return group.converse() - padConverse;
  }

  @Override
  public double converseSpan() {
    return converseSpan + padConverse + padConverseEnd;
  }

  @Override
  public double transverse() {
    return baselineTransverse - ascent();
  }

  @Override
  public double transverseSpan() {
    return ascent() + descent();
  }

  @Override
  public double transverseEdge() {
    return baselineTransverse + descent();
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    group.setConverse(converse + padConverse, animate);
  }

  @Override
  public Object inner_() {
    return group.inner_();
  }

  public void add(CourseDisplayNode node) {
    if (node.ascent() > ascent) {
      ascent = node.ascent();
      updateLocation(false);
    }
    if (node.descent() > descent) {
      descent = node.descent();
    }
    node.setConverse(converseSpan);
    converseSpan += node.converseSpan();
    group.add(node);
  }
}
