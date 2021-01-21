package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;

public class FreeAtomType extends AtomType {
  public final String name;
  public final int depthScore;
  public final ROMap<String, AlignmentSpec> alignments;
  public final int precedence;
  public final boolean associateForward;
  public final int autoChooseAmbiguity;

  public static class Config {
    public AtomType.Config base;
    public String name;
    public int depthScore = 0;
    public ROMap<String, AlignmentSpec> alignments = ROMap.empty;
    public int precedence = Integer.MAX_VALUE;
    public boolean associateForward = false;
    public int autoChooseAmbiguity = 1;

    public Config() {}

    public Config(
        AtomType.Config base,
        String name,
        int depthScore,
        ROMap<String, AlignmentSpec> alignments,
        int precedence,
        boolean associateForward,
        int autoChooseAmbiguity) {
      this.base = base;
      this.name = name;
      this.depthScore = depthScore;
      this.alignments = alignments;
      this.precedence = precedence;
      this.associateForward = associateForward;
      this.autoChooseAmbiguity = autoChooseAmbiguity;
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
