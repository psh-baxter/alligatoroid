package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.Map;
import java.util.Set;

public class MiddleAtomSpec extends MiddleSpec {

  public String type;

  public ValueAtom get(final Map<String, com.zarbosoft.merman.document.values.Value> data) {
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
