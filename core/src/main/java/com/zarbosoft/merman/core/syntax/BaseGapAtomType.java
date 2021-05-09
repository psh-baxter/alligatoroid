package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class BaseGapAtomType extends AtomType {
  public static final String PRIMITIVE_KEY = "gap";

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
