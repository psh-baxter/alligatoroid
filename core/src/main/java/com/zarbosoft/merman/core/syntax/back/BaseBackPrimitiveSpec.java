package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.primitivepattern.Pattern;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class BaseBackPrimitiveSpec extends BackSpecData {
  public final Pattern.Matcher matcher;

  public static class Config {
    public final String id;
    public final Pattern pattern;

    public Config(String id, Pattern pattern) {
      this.id = id;
      this.pattern = pattern;
    }
  }

  protected BaseBackPrimitiveSpec(Config config) {
    super(config.id);
    if (config.pattern != null) matcher = new Pattern.Matcher(config.pattern);
    else matcher = null;
  }

  public FieldPrimitive get(final ROMap<String, Field> data) {
    return (FieldPrimitive) data.getOpt(id);
  }
}
