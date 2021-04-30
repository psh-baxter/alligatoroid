package com.zarbosoft.merman.core.display.derived;

import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;

public class CourseGroup implements CourseDisplayNode {
  private final Group group;
  private double baselineTransverse;
  private double ascent;
  private double descent;

  public CourseGroup(Group group) {
    this.group = group;
  }

  @Override
  public void setBaselineTransverse(double transverse, boolean animate) {
    baselineTransverse = transverse;
    updateLocation(animate);
  }

  private void updateLocation(boolean animate) {
    group.setTransverse(baselineTransverse + ascent, animate);
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    baselineTransverse = vector.transverse;
    group.setPosition(new Vector(vector.converse, baselineTransverse + ascent), animate);
  }

  @Override
  public double baselineTransverse() {
    return baselineTransverse;
  }

  @Override
  public double ascent() {
    return ascent;
  }

  @Override
  public double descent() {
    return descent;
  }

  @Override
  public double converse() {
    return group.converse();
  }

  @Override
  public double converseSpan() {
    return group.converseSpan();
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    group.setConverse(converse, animate);
  }

  public void add(CourseDisplayNode node) {
    if (node.ascent() > ascent) {
      ascent = node.ascent();
      updateLocation(false);
    }
    if (node.descent() > descent) {
      descent = node.descent();
    }
    group.add(node);
  }
}
