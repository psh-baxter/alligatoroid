package com.zarbosoft.merman.core.syntax.builder;

import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedMap;
import com.zarbosoft.rendaw.common.TSOrderedSet;
import com.zarbosoft.rendaw.common.TSSet;

public class BackFixedRecordSpecBuilder {
  private final TSOrderedMap<String, BackSpec> pairs = new TSOrderedMap<>();
  private final TSSet<String> discard = new TSSet<>();

  public BackFixedRecordSpecBuilder field(String key, BackSpec spec) {
    pairs.putNew(key, spec);
    return this;
  }

  public BackFixedRecordSpecBuilder discardField(String key) {
    discard.addNew(key);
    return this;
  }

  public BackSpec build() {
    return new BackFixedRecordSpec(new BackFixedRecordSpec.Config(pairs, discard.ro()));
  }
}
