package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.rendaw.common.ROMap;

public class FreeAtomType extends AtomType {
  public final String name;
  public final int depthScore;
  public final ROMap<String, AlignmentSpec> alignments;
  public final int precedence;
  public final boolean associateForward;
  public final int autoChooseAmbiguity;

  public static class Config {
    public final String name;
    private final AtomType.Config base;
    public int depthScore = 0;
    public ROMap<String, AlignmentSpec> alignments = ROMap.empty;
    public int precedence = Integer.MAX_VALUE;
    public boolean associateForward = false;
    public int autoChooseAmbiguity = 1;

    public Config(String name, AtomType.Config base) {
      this.name = name;
      this.base = base;
    }

    public Config depthScore(int score) {
      this.depthScore =  score;
      return this;
    }

    public Config precedence(int val) {
      this.precedence = val;
      return this;
    }

    public Config associateForward(boolean yes) {
      associateForward = yes;
      return this;
    }

    public Config autoChooseAmbiguity(int val) {
      autoChooseAmbiguity =val;
      return this;
    }

    public Config alignments(ROMap<String, AlignmentSpec> alignments) {
      this.alignments = alignments;
      return this;
    }
  }

  public FreeAtomType(Config config) {
    super(config.base);
    this.name = config.name;
    this.depthScore = config.depthScore;
    this.alignments = config.alignments;
    this.precedence = config.precedence;
    this.associateForward = config.associateForward;
    this.autoChooseAmbiguity = config.autoChooseAmbiguity;
  }

  @Override
  public ROMap<String, AlignmentSpec> alignments() {
    return alignments;
  }

  @Override
  public int precedence() {
    return precedence;
  }

  @Override
  public boolean associateForward() {
    return associateForward;
  }

  @Override
  public int depthScore() {
    return depthScore;
  }

  @Override
  public String name() {
    return name;
  }
}
