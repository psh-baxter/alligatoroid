package com.zarbosoft.merman.core.syntax.builder;

import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.rendaw.common.TSList;

public class FrontArraySpecBuilder {
  private final TSList<FrontSymbol> prefix = new TSList<>();
  private final TSList<FrontSymbol> suffix = new TSList<>();
  private final TSList<FrontSymbol> separator = new TSList<>();
  private final String back;

  public FrontArraySpecBuilder(String back) {
    this.back = back;
  }

  public FrontArraySpecBuilder prefix(FrontSymbol symbol) {
    prefix.add(symbol);
    return this;
  }

  public FrontArraySpecBuilder suffix(FrontSymbol symbol) {
    suffix.add(symbol);
    return this;
  }

  public FrontArraySpecBuilder separator(FrontSymbol symbol) {
    separator.add(symbol);
    return this;
  }

  public FrontArraySpec build() {
    FrontArraySpecBase.Config baseConfig = new FrontArraySpecBase.Config();
    baseConfig.prefix = prefix;
    baseConfig.suffix = suffix;
    baseConfig.separator = separator;
    return new FrontArraySpec(new FrontArraySpec.Config(back, baseConfig));
  }
}
