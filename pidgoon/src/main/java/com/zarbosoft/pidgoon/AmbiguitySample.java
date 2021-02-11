package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.model.Position;

public class AmbiguitySample {
  public int step;
  public int ambiguity;
  public Position position;
  public int duplicates;

  public AmbiguitySample(
      final int step, final int ambiguity, final Position position, final int duplicates) {
    this.step = step;
    this.ambiguity = ambiguity;
    this.position = position;
    this.duplicates = duplicates;
  }

  public AmbiguitySample() {}
}
