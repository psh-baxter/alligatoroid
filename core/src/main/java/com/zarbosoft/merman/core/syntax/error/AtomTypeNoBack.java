package com.zarbosoft.merman.core.syntax.error;

public class AtomTypeNoBack extends BaseKVError{
  public AtomTypeNoBack() {
  }

  @Override
  protected String description() {
    return "atom type, no back";
  }
}
