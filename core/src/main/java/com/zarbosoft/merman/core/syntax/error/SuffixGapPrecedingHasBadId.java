package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;

public class SuffixGapPrecedingHasBadId extends BaseKVError {
  public SuffixGapPrecedingHasBadId(String gapId, String id) {
    put("gap", gapId);
    put("id", id);
    put("must_be", SuffixGapAtomType.PRECEDING_KEY);
  }

  @Override
  protected String description() {
    return "suffix gap preceding has incorrect id";
  }
}
