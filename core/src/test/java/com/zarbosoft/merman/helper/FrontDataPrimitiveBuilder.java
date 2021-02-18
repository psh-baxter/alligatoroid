package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.style.Style;

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
    if (alignment != null) {
      config.firstStyle(c -> c.alignment(alignment));
      config.softStyle(c -> c.alignment(alignment));
      config.hardStyle(c -> c.alignment(alignment));
    }
    if (compactAlignment != null) {
      config.firstStyle(c -> c.splitAlignment(compactAlignment));
      config.softStyle(c -> c.splitAlignment(compactAlignment));
      config.hardStyle(c -> c.splitAlignment(compactAlignment));
    }
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
