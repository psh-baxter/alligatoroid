package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.ArrayMissingAtom;
import com.zarbosoft.merman.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.rendaw.common.Common;

import java.util.List;

public abstract class BaseBackSimpleArraySpec extends BaseBackArraySpec {
  public BackSpec element;
  public BaseBackAtomSpec elementAtom;

  @Override
  public String elementAtomType() {
    return elementAtom.type;
  }

  @Override
  public void finish(
    List<Object> errors,
    final Syntax syntax,
    final Path typePath,
    final TSMap<String, BackSpecData> fields,
    boolean singularRestriction, boolean typeRestriction
  ) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    Common.Mutable<BackAtomSpec> atom = new Common.Mutable<>();
    BackSpec.walk(
        element,
        child -> {
          if (!(child instanceof BackAtomSpec)) return;
          if (atom.value != null) {
            errors.add(new ArrayMultipleAtoms(this, atom.value, child));
          } else {
            atom.value = (BackAtomSpec) child;
          }
        });
    if (atom.value == null) {
      errors.add(new ArrayMissingAtom(this));
    }
    elementAtom = atom.value;
    element.finish(errors, syntax, typePath.add("element"), null, false, false);
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }
}
