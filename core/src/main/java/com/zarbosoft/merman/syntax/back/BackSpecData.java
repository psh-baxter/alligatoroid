package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.List;

public abstract class BackSpecData extends BackSpec {
  public String id;

  public abstract Value create(Syntax syntax);

  @Override
  public void finish(
      List<Object> errors,
      Syntax syntax,
      Path typePath,
      TSMap<String, BackSpecData> fields,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    if (fields != null) fields.put(id, this);
  }
}
