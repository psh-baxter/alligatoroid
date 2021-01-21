package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;

public class FrontSpaceBuilder {
  private final TSSet<String> tags = new TSSet<>();

  public FrontSpaceBuilder() {}

  public FrontSymbol build() {
    return new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(), null, null, tags.ro()));
  }

  public FrontSpaceBuilder tag(final String tag) {
    tags.add(tag);
    return this;
  }
}
