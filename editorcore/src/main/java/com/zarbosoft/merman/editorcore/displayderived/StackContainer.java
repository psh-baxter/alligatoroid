package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;

public class StackContainer implements Container, FreeDisplayNode {
  private final Group group;
  public Container root;

  public StackContainer(Context context) {
    this.group = context.display.group();
  }

  public StackContainer add(FreeDisplayNode node) {
    this.group.add(node);
    return this;
  }

  public StackContainer addRoot(Container node) {
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

  @Override
  public void setConverseSpan(Context context, double span) {
    root.setConverseSpan(context, span);
  }
}
