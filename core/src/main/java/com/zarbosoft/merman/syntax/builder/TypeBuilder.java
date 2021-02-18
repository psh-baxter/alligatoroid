package com.zarbosoft.merman.syntax.builder;

import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class TypeBuilder {
  private final String id;
  private final TSList<BackSpec> back = new TSList<>();
  private final TSList<FrontSpec> front = new TSList<>();
  private final String humanName;
  private final TSMap<String, AlignmentSpec> alignments = new TSMap<>();
  private int depthScore = 1;
  private int precedence = Integer.MAX_VALUE;
  private boolean associateForward = false;
  private int autoChooseAmbiguity = 1;

  public TypeBuilder(String id, String humanName) {
    this.id = id;
    this.humanName = humanName;
  }

  public TypeBuilder back(BackSpec spec) {
    back.add(spec);
    return this;
  }

  public TypeBuilder front(FrontSpec spec) {
    front.add(spec);
    return this;
  }

  public TypeBuilder precedence(int precedence) {
    this.precedence = precedence;
    return this;
  }

  public TypeBuilder associateForward() {
    this.associateForward = true;
    return this;
  }

  public TypeBuilder alignment(String key, AlignmentSpec spec) {
    alignments.putNew(key, spec);
    return this;
  }

  public FreeAtomType build() {
    return new FreeAtomType(
        new FreeAtomType.Config(humanName, new AtomType.Config(id, back.mut(), front.mut()))
            .depthScore(depthScore)
            .alignments(alignments)
            .precedence(precedence)
            .associateForward(associateForward)
            .autoChooseAmbiguity(autoChooseAmbiguity));
  }
}
