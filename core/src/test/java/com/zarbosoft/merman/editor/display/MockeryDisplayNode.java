package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.core.display.DisplayNode;

public abstract class MockeryDisplayNode implements DisplayNode {
  public double converse;

  @Override
  public double converse() {
    return converse;
  }

  @Override
  public void setConverse(final double converse, final boolean animate) {
    this.converse = converse;
  }
}
