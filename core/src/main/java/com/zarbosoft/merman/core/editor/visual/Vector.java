package com.zarbosoft.merman.core.editor.visual;

import com.zarbosoft.rendaw.common.Format;

public class Vector {
  public final double converse;
  public final double transverse;

  public Vector(final double converse, final double transverse) {
    this.converse = converse;
    this.transverse = transverse;
  }

  @Override
  public String toString() {
    return Format.format("<%s, %s>", converse, transverse);
  }

  public Vector add(final Vector other) {
    return new Vector(converse + other.converse, transverse + other.transverse);
  }

  public Vector setTransverse(final double transverse) {
    return new Vector(converse, transverse);
  }

  public Vector addTransverse(final double transverse) {
    return new Vector(converse, this.transverse + transverse);
  }

  public Vector setConverse(final double converse) {
    return new Vector(converse, transverse);
  }

  public Vector addConverse(final double converse) {
    return new Vector(this.converse + converse, transverse);
  }

  public boolean lessThan(final Vector other) {
    return transverse < other.transverse
        || (transverse == other.transverse && converse < other.converse);
  }

  public boolean greaterThan(final Vector other) {
    return transverse > other.transverse
        || (transverse == other.transverse && converse > other.converse);
  }
}
