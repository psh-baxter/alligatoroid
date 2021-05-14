package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedMap;
import com.zarbosoft.rendaw.common.TSOrderedSet;

public class BackRecordBuilder {
  private final TSOrderedMap<String, BackSpec> pairs = new TSOrderedMap<>();

  public BackRecordBuilder add(final String key, final BackSpec part) {
    pairs.put(key, part);
    return this;
  }

  public BackSpec build() {
    return new BackFixedRecordSpec(new BackFixedRecordSpec.Config(pairs, ROSet.empty));
  }
}
