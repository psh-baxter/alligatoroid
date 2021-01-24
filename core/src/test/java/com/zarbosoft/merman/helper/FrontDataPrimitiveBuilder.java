package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontDataPrimitiveBuilder {
  private final String field;
  private final TSSet<String> tags = new TSSet<>();

  public FrontDataPrimitiveBuilder(final String field) {
    this.field = field;
  }

  public FrontPrimitiveSpec build() {
    return new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config(field, tags.ro()));
  }

  public FrontDataPrimitiveBuilder tag(final String tag) {
    tags.add(tag);
    return this;
  }
}
