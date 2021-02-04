package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.merman.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.RelativeAlignment;

public class RelativeAlignmentSpec implements AlignmentSpec {
  public final String base;
  public final int offset;
  public final boolean collapse;

  public static class Config {
    public final String base;
    public final int offset;
    public final boolean collapse;

    public Config(String base, int offset, boolean collapse) {
      this.base = base;
      this.offset = offset;
      this.collapse = collapse;
    }
  }

  public RelativeAlignmentSpec(Config config) {
    this.base = config.base;
    this.offset = config.offset;
    this.collapse = config.collapse;
  }

  @Override
  public Alignment create() {
    return new RelativeAlignment(base, offset, collapse);
  }
}
