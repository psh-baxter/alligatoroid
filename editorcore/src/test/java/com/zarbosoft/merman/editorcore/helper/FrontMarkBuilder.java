package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;

public class FrontMarkBuilder {
  private final String text;
  private Style.SplitMode splitMode;

  public FrontMarkBuilder(final String value) {
    text = value;
  }

  public FrontSymbolSpec build() {
    SymbolTextSpec.Config config = new SymbolTextSpec.Config(text);
    if (splitMode != null) config.splitMode(splitMode);
    return new FrontSymbolSpec(
        new FrontSymbolSpec.Config(new SymbolTextSpec(config)));
  }

  public FrontMarkBuilder split(Style.SplitMode compact) {
    this.splitMode = compact;
    return this;
  }
}
