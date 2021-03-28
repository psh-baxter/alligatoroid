package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;

public class RootAtomType extends AtomType {
  public static final String ROOT_TYPE_ID = "root";
  private final ROMap<String, AlignmentSpec> alignments;

  public RootAtomType(Config config) {
    super(new AtomType.Config(ROOT_TYPE_ID, config.back, config.front));
    alignments = config.alignments;
  }

  @Override
  public ROMap<String, AlignmentSpec> alignments() {
    return alignments;
  }

  @Override
  public int precedence() {
    return -Integer.MAX_VALUE;
  }

  @Override
  public boolean associateForward() {
    return false;
  }

  @Override
  public int depthScore() {
    return 0;
  }

  @Override
  public String name() {
    return "root array";
  }

  public static class Config {
    public final ROList<BackSpec> back;
    public final ROList<FrontSpec> front;
    public final ROMap<String, AlignmentSpec> alignments;

    public Config(
        ROList<BackSpec> back, ROList<FrontSpec> front, ROMap<String, AlignmentSpec> alignments) {
      this.back = back;
      this.front = front;
      this.alignments = alignments;
    }
  }
}
