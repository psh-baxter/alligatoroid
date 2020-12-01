package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.Set;

public abstract class BaseBackAtomSpec extends BackSpecData{
  public String type;

  public ValueAtom get(final TSMap<String, Value> data) {
    return (ValueAtom) data.get(id);
  }

  @Override
  public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
    if (type == null) return; // Gaps have null type, take anything
    if (!scalarTypes.contains(type))
      throw new InvalidSyntax(String.format("Unknown type [%s].", type));
  }

  @Override
  public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
    return new ValueAtom(this, syntax.gap.create());
  }
}
