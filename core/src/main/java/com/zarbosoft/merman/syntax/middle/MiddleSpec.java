package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.Set;

public abstract class MiddleSpec {
  public String id;

  public abstract void finish(Set<String> allTypes, Set<String> scalarTypes);

  public abstract Value create(Syntax syntax);
}
