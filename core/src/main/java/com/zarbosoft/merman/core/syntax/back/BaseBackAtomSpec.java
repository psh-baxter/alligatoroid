package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.AtomCandidatePluralBack;
import com.zarbosoft.merman.core.syntax.error.AtomCandidateTypeNotAllowed;
import com.zarbosoft.merman.core.syntax.error.AtomTypeDoesntExist;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSMap;

public abstract class BaseBackAtomSpec extends BackSpecData {
  /** Type/group name or null; null means any type */
  public final String type;

  public static class Config {
    public final String type;
    public final String id;

    public Config(String id, String type) {
      this.type = type;
      this.id = id;
    }
  }

  public BaseBackAtomSpec(Config config) {
    super(config.id);
    this.type = config.type;
  }

  public FieldAtom get(final TSMap<String, Field> data) {
    return (FieldAtom) data.getOpt(id);
  }

  @Override
  public void finish(
      MultiError errors,
      Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (type == null) return; // Gaps have null type, take anything
    ROSet<AtomType> childTypes = syntax.splayedTypes.getOpt(type);
    if (childTypes == null) {
      errors.add(new AtomTypeDoesntExist(typePath, type));
    } else {
      for (final AtomType child : childTypes) {
        if (singularRestriction && child.back().size() > 1) {
          errors.add(new AtomCandidatePluralBack(typePath, child, child.back().size()));
        }
        if (typeRestriction && child.back().get(0).isTypedValue()) {
          errors.add(new AtomCandidateTypeNotAllowed(typePath, child));
        }
      }
    }
  }
}
