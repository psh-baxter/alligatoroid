package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.style.Style;

public class FrontDataPrimitiveBuilder {
  private final String field;
  private Style.SplitMode splitMode;
  private String compactAlignment;
  private String alignment;

  public FrontDataPrimitiveBuilder(final String field) {
    this.field = field;
  }

  public FrontPrimitiveSpec build() {
    FrontPrimitiveSpec.Config config = new FrontPrimitiveSpec.Config(field);
    if (splitMode != null)  {
      config.splitMode(splitMode);
    }
    Style.Config style = new Style.Config();
    if (alignment != null) {
      style.alignment(alignment);
    }
    if (compactAlignment != null) {
      style.splitAlignment(compactAlignment);
    }
      config.style(new Style(style));
    return new FrontPrimitiveSpec(config);
  }

  public FrontDataPrimitiveBuilder split(Style.SplitMode splitMode) {
    this.splitMode = splitMode;
    return this;
  }

  public FrontDataPrimitiveBuilder alignment(String alignment) {
    this.alignment = alignment;
    return this;
  }
  public FrontDataPrimitiveBuilder compactAlignment(String compactAlignment) {
    this.compactAlignment = compactAlignment;
    return this;
  }
}
