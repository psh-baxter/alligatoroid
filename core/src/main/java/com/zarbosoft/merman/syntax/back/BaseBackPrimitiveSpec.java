package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.primitivepattern.Pattern;

import java.util.Set;

public abstract class BaseBackPrimitiveSpec extends BackSpecData {
  public Pattern pattern = null;

  public Pattern.Matcher matcher = null;

  public ValuePrimitive get(final TSMap<String, Value> data) {
    return (ValuePrimitive) data.get(id);
  }

  @Override
  public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
    if (pattern != null) matcher = pattern.new Matcher();
  }

  @Override
  public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
    return new ValuePrimitive(this, "");
  }
}
