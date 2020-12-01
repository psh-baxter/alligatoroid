package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.Set;

public abstract class BackSpecData extends BackSpec{
  public String id;

  public abstract void finish(Set<String> allTypes, Set<String> scalarTypes);

  public abstract Value create(Syntax syntax);
}
