package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.AtomType;

public class AtomCandidateTypeNotAllowed extends BaseKVError{
  public AtomCandidateTypeNotAllowed(SyntaxPath typePath, AtomType candidate) {
      put("typePath", typePath);
      put("candidate", candidate);
  }

  @Override
  protected String description() {
    return "this candidate would result in directly nested types (invalid luxem)";
  }
}
