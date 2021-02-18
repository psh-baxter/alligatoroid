package com.zarbosoft.merman.standalone.helper;

import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontMarkBuilder {
  private final TSSet<String> tags = new TSSet<>();
  private final String type;

  public FrontMarkBuilder(final String value) {
    type = value;
  }

  public FrontSymbol build() {
    return new FrontSymbol(new FrontSymbol.Config(new SymbolTextSpec(type, style)));
  }

  public FrontMarkBuilder tag(final String tag) {
    tags.add(tag);
    return this;
  }
}
