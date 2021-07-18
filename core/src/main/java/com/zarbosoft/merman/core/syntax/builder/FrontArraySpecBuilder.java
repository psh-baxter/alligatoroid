package com.zarbosoft.merman.core.syntax.builder;

import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.rendaw.common.TSList;

public class FrontArraySpecBuilder {
  private final TSList<FrontSymbolSpec> prefix = new TSList<>();
  private final TSList<FrontSymbolSpec> suffix = new TSList<>();
  private final TSList<FrontSymbolSpec> separator = new TSList<>();
  private final String back;

  public FrontArraySpecBuilder(String back) {
    this.back = back;
  }

  public FrontArraySpecBuilder prefix(FrontSymbolSpec symbol) {
    prefix.add(symbol);
    return this;
  }

  public FrontArraySpecBuilder suffix(FrontSymbolSpec symbol) {
    suffix.add(symbol);
    return this;
  }

  public FrontArraySpecBuilder separator(FrontSymbolSpec symbol) {
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
