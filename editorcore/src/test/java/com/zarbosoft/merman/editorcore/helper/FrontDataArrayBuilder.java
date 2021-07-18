package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.rendaw.common.TSList;

public class FrontDataArrayBuilder {
  private final TSList<FrontSymbolSpec> prefix = new TSList<>();
  private final TSList<FrontSymbolSpec> separator = new TSList<>();
  private final String field;

  public FrontDataArrayBuilder(final String field) {
    this.field = field;
  }

  public FrontArraySpec build() {
    FrontArraySpecBase.Config base = new FrontArraySpecBase.Config();
    base.prefix = prefix;
    base.separator = separator;
    return new FrontArraySpec(new FrontArraySpec.Config(field, base));
  }

  public FrontDataArrayBuilder addSeparator(final FrontSymbolSpec part) {
    separator.add(part);
    return this;
  }

  public FrontDataArrayBuilder addPrefix(final FrontSymbolSpec part) {
    prefix.add(part);
    return this;
  }
}
