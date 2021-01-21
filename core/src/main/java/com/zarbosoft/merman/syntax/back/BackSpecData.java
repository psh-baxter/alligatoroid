package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.List;

public abstract class BackSpecData extends BackSpec {
  public final String id;

  protected BackSpecData(String id) {
    this.id = id;
  }
}
