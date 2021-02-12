package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.rendaw.common.TSMap;

public abstract class BaseBackArraySpec extends BackSpecData {

  protected BaseBackArraySpec(String id) {
    super(id);
  }

  public abstract String elementAtomType();

  public Path getPath(final ValueArray value, final int actualIndex) {
    return value.getSyntaxPath().add(Integer.toString( actualIndex));
  }

  public ValueArray get(final TSMap<String, Value> data) {
    return (ValueArray) data.getOpt(id);
  }
}
