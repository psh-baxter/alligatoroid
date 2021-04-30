package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.fields.Field;

public abstract class BackSpecData extends BackSpec {
  public final String id;

  protected BackSpecData(String id) {
    this.id = id;
  }
}
