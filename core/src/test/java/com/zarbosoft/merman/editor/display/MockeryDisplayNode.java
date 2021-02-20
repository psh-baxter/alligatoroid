package com.zarbosoft.merman.editor.display;

public abstract class MockeryDisplayNode implements DisplayNode {
  public int converse;

  @Override
  public int converse() {
    return converse;
  }

  @Override
  public void setConverse(final int converse, final boolean animate) {
    this.converse = converse;
  }
}
