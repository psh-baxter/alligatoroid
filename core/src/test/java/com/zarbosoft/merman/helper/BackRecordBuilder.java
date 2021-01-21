package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;

public class BackRecordBuilder {
  private final TSMap<String, BackSpec> pairs = new TSMap<>();

  public BackRecordBuilder add(final String key, final BackSpec part) {
    pairs.put(key, part);
    return this;
  }

  public BackSpec build() {
    return new BackFixedRecordSpec(pairs);
  }
}
