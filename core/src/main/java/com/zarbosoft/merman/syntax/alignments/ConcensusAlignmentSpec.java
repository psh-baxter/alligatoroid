package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.ConcensusAlignment;

public class ConcensusAlignmentSpec implements AlignmentSpec {
  @Override
  public Alignment create() {
    return new ConcensusAlignment();
  }
}
