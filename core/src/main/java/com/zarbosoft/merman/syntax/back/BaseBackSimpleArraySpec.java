package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.ArrayMissingAtom;
import com.zarbosoft.merman.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.rendaw.common.Common;

public abstract class BaseBackSimpleArraySpec extends BaseBackArraySpec {
  public final BackSpec element;
  public final BaseBackAtomSpec elementAtom;

  public static class Config {
    public final String id;
    public final BackSpec element;

    public Config(String id, BackSpec element) {
      this.id = id;
      this.element = element;
    }
  }

  protected BaseBackSimpleArraySpec(Config config) {
    super(config.id);
    this.element = config.element;
    MultiError errors = new MultiError();
    final BackAtomSpec[] atom = {null};
    BackSpec.walk(
        element,
        child -> {
          if (!(child instanceof BackAtomSpec)) return true;
          if (atom[0] != null) {
            errors.add(new ArrayMultipleAtoms(this, atom[0], child));
          } else {
            atom[0] = (BackAtomSpec) child;
          }
          return false;
        });
    if (atom[0] == null) {
      errors.add(new ArrayMissingAtom(this));
    }
    elementAtom = atom[0];
    errors.raise();
  }

  @Override
  public String elementAtomType() {
    return elementAtom.type;
  }

  @Override
  public void finish(
      MultiError errors,
      final Syntax syntax,
      final Path typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    element.finish(errors, syntax, typePath.add("element"), false, false);
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }
}
