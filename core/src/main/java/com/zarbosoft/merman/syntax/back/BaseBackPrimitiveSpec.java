package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.primitivepattern.Pattern;

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
    if (config.pattern != null) matcher = config.pattern.new Matcher();
    else matcher = null;
  }

  public ValuePrimitive get(final ROMap<String, Value> data) {
    return (ValuePrimitive) data.getOpt(id);
  }
}
