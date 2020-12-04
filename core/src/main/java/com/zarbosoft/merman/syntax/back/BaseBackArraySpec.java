package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;

public abstract class BaseBackArraySpec extends BackSpecData {
  public FrontArraySpecBase front;

  public abstract String elementAtomType();

  public Path getPath(final ValueArray value, final int actualIndex) {
    return value.getSyntaxPath().add(String.format("%d", actualIndex));
  }

  @Override
  public Value create(final Syntax syntax) {
    return new ValueArray(this);
  }

  public ValueArray get(final TSMap<String, Value> data) {
    return (ValueArray) data.getOpt(id);
  }
}
