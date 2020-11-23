package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;

import java.util.Map;
import java.util.Set;

public abstract class MiddleArraySpecBase extends MiddleSpec {

  public String type;
  public FrontArraySpecBase front;

  public abstract Path getPath(final ValueArray value, final int actualIndex);

  @Override
  public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
    if (type != null && !allTypes.contains(type))
      throw new InvalidSyntax(String.format("Unknown type [%s].", type));
  }

  @Override
  public Value create(final Syntax syntax) {
    return new ValueArray(this);
  }

  public ValueArray get(final Map<String, Value> data) {
    return (ValueArray) data.get(id);
  }
}
