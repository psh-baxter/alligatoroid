package com.zarbosoft.merman.core.syntax.alignments;

import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.alignment.ConcensusAlignment;

public class ConcensusAlignmentSpec implements AlignmentSpec {
  @Override
  public Alignment create() {
    return new ConcensusAlignment();
  }
}
