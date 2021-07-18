package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.primitivepattern.Pattern;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class BaseBackPrimitiveSpec extends BackSpecData {
  public final Pattern pattern;
  public final Pattern.Matcher matcher;

  protected BaseBackPrimitiveSpec(Config config) {
    super(config.id);
    pattern = config.pattern;
    if (config.pattern != null) {
      matcher = new Pattern.Matcher(config.pattern);
    } else {
      matcher = null;
    }
  }

  public FieldPrimitive get(final ROMap<String, Field> data) {
    return (FieldPrimitive) data.getOpt(id);
  }

  public static class Config {
    public final String id;
    public Pattern pattern;

    public Config(String id) {
      this.id = id;
    }

    public Config pattern(Pattern pattern) {
      this.pattern = pattern;
      return this;
    }
  }
}
