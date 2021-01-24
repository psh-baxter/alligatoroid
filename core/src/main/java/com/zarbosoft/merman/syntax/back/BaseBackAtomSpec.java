package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.AtomCandidatePluralBack;
import com.zarbosoft.merman.syntax.error.AtomCandidateTypeNotAllowed;
import com.zarbosoft.merman.syntax.error.AtomTypeDoesntExist;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Set;

public abstract class BaseBackAtomSpec extends BackSpecData {
  /** Type/group name or null; null means any type */
  public final String type;

  public static class Config {
    public final String type;
    public final String id;

    public Config(String type, String id) {
      this.type = type;
      this.id = id;
    }
  }

  public BaseBackAtomSpec(Config config) {
    super(config.id);
    this.type = config.type;
  }

  public ValueAtom get(final TSMap<String, Value> data) {
    return (ValueAtom) data.getOpt(id);
  }

  @Override
  public void finish(
      MultiError errors,
      Syntax syntax,
      Path typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (type == null) return; // Gaps have null type, take anything
    Set<AtomType> childTypes = syntax.splayedTypes.getOpt(type);
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
