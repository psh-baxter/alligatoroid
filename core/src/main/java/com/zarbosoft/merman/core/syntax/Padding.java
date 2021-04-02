package com.zarbosoft.merman.core.syntax;

public class Padding {
  public static final Padding empty = new Padding(0, 0, 0, 0);
  public final double converseStart;
  public final double converseEnd;
  public final double transverseStart;
  public final double transverseEnd;

  public Padding(
      double converseStart, double converseEnd, double transverseStart, double transverseEnd) {
    this.converseStart = converseStart;
    this.converseEnd = converseEnd;
    this.transverseStart = transverseStart;
    this.transverseEnd = transverseEnd;
  }

  public static Padding same(double amount) {
    return new Padding(amount, amount, amount, amount);
  }
}
