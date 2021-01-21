package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.RelativeAlignment;

public class RelativeAlignmentSpec implements AlignmentSpec {

  public final String base;

  public final int offset;

  public static class Config {
    public final String base;
    public final int offset;

    public Config(String base, int offset) {
      this.base = base;
      this.offset = offset;
    }
  }

  public RelativeAlignmentSpec(Config config) {
    this.base = config.base;
    this.offset = config.offset;
  }

  @Override
  public Alignment create() {
    return new RelativeAlignment(base, offset);
  }
}
