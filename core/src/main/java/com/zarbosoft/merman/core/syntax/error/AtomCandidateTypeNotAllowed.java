package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.syntax.AtomType;

public class AtomCandidateTypeNotAllowed extends BaseKVError{
  public AtomCandidateTypeNotAllowed(Path typePath, AtomType candidate) {
      put("typePath", typePath);
      put("candidate", candidate);
  }

  @Override
  protected String description() {
    return "this candidate would result in directly nested types (invalid luxem)";
  }
}
