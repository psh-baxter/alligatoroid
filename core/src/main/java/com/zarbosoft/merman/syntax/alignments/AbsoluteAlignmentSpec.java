package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.AbsoluteAlignment;

public class AbsoluteAlignmentSpec implements AlignmentSpec {

  public final int offset;

  public AbsoluteAlignmentSpec(int offset) {
    this.offset = offset;
  }

  @Override
  public Alignment create() {
    return new AbsoluteAlignment(offset);
  }
}
