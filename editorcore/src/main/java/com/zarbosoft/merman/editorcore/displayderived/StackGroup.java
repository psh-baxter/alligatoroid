package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;

public class StackGroup implements FreeDisplayNode {
  private final Group group;
  public FreeDisplayNode root;

  public StackGroup(Context context) {
    this.group = context.display.group();
  }

  public StackGroup add(FreeDisplayNode node) {
    this.group.add(node);
    return this;
  }

  public StackGroup addRoot(FreeDisplayNode node) {
    this.group.add(node);
    this.root = node;
    return this;
  }

  @Override
  public double converse() {
    return group.converse();
  }

  @Override
  public double transverse() {
    return group.transverse();
  }

  @Override
  public double transverseSpan() {
    return root.transverseSpan();
  }

  @Override
  public double converseSpan() {
    return root.converseSpan();
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    group.setConverse(converse, animate);
  }

  @Override
  public Object inner_() {
    return group.inner_();
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    group.setPosition(vector, animate);
  }
}
