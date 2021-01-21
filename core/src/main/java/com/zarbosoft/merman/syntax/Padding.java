package com.zarbosoft.merman.syntax;

public class Padding {
  public static final Padding empty = new Padding(0, 0, 0, 0);
  public final int converseStart;
  public final int converseEnd;
  public final int transverseStart;
  public final int transverseEnd;

  public Padding(int converseStart, int converseEnd, int transverseStart, int transverseEnd) {
    this.converseStart = converseStart;
    this.converseEnd = converseEnd;
    this.transverseStart = transverseStart;
    this.transverseEnd = transverseEnd;
  }
}
