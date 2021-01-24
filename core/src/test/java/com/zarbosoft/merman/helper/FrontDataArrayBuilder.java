package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.rendaw.common.TSList;

public class FrontDataArrayBuilder {
  private final TSList<FrontSymbol> prefix = new TSList<>();
  private final TSList<FrontSymbol> separator = new TSList<>();
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

  public FrontDataArrayBuilder addSeparator(final FrontSymbol part) {
    separator.add(part);
    return this;
  }

  public FrontDataArrayBuilder addPrefix(final FrontSymbol part) {
    prefix.add(part);
    return this;
  }
}
