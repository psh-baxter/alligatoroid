package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontMarkBuilder {
  private final TSSet<String> tags = new TSSet<>();
  private final String text;
  private Style.SplitMode splitMode;

  public FrontMarkBuilder(final String value) {
    text = value;
  }

  public FrontSymbol build() {
    SymbolTextSpec.Config config = new SymbolTextSpec.Config(text);
    if (splitMode != null) config.splitMode(splitMode);
    return new FrontSymbol(
        new FrontSymbol.Config(new SymbolTextSpec(config)));
  }

  public FrontMarkBuilder tag(final String tag) {
    tags.add(tag);
    return this;
  }

  public FrontMarkBuilder split(Style.SplitMode compact) {
    this.splitMode = compact;
    return this;
  }
}
