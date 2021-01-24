package com.zarbosoft.merman.editorcore.editing;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;

public abstract class BaseGapAtomType extends AtomType {
  public static final String GAP_PRIMITIVE_KEY = "gap";
  private final String id;
  public final EditingExtension edit;

  public BaseGapAtomType(EditingExtension edit, String id) {
    this.edit = edit;
    this.id = id;
  }

  @Override
  public final ROMap<String, AlignmentSpec> alignments() {
    return ImmutableMap.of();
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

  @Override
  public final String id() {
    return id;
  }
}
