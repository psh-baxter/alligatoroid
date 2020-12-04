package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.FreeAtomType;

public class AtomCandidateTypeNotAllowed extends BaseKVError{
  public AtomCandidateTypeNotAllowed(Path typePath, FreeAtomType candidate) {
    super(ImmutableMap.<String, Object>builder()
      .put("typePath", typePath)
      .put("candidate", candidate)
      .build());
  }

  @Override
  protected String name() {
    return "this candidate would result in directly nested types (invalid luxem)";
  }
}
