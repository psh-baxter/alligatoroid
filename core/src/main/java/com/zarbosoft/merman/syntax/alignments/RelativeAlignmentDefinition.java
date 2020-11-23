package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.RelativeAlignment;

public class RelativeAlignmentDefinition implements AlignmentDefinition {

  public String base;

  public int offset;

  @Override
  public Alignment create() {
    return new RelativeAlignment(base, offset);
  }
}
