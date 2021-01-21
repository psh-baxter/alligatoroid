package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;

public abstract class BaseGapAtomType extends AtomType {
  public static final String GAP_PRIMITIVE_KEY = "gap";

  public BaseGapAtomType(Config config) {
    super(config);
  }

  @Override
  public final ROMap<String, AlignmentSpec> alignments() {
    return ROMap.empty;
  }

  @Override
  public final int precedence() {
    return 1_000_000;
  }

  @Override
  public final boolean associateForward() {
    return false;
  }

  @Override
  public final int depthScore() {
    return 0;
  }
}
