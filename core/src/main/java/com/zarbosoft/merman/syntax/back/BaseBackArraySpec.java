package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Field;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.rendaw.common.TSMap;

public abstract class BaseBackArraySpec extends BackSpecData {

  protected BaseBackArraySpec(String id) {
    super(id);
  }

  public abstract String elementAtomType();

  public Path getPath(final FieldArray value, final int actualIndex) {
    return value.getSyntaxPath().add(Integer.toString( actualIndex));
  }

  public FieldArray get(final TSMap<String, Field> data) {
    return (FieldArray) data.getOpt(id);
  }
}
