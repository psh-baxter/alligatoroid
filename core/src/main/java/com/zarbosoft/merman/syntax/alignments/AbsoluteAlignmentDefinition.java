package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.AbsoluteAlignment;

public class AbsoluteAlignmentDefinition implements AlignmentDefinition {

  public int offset;

  @Override
  public Alignment create() {
    return new AbsoluteAlignment(offset);
  }
}
