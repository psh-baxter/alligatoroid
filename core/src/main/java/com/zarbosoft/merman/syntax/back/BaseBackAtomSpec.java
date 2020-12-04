package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.AtomCandidatePluralBack;
import com.zarbosoft.merman.syntax.error.AtomCandidateTypeNotAllowed;
import com.zarbosoft.merman.syntax.error.AtomTypeDoesntExist;

import java.util.List;

public abstract class BaseBackAtomSpec extends BackSpecData{
  /**
   * Type/group name or null; null means any type
   */
  public String type;

  public ValueAtom get(final TSMap<String, Value> data) {
    return (ValueAtom) data.getOpt(id);
  }

  @Override
  public void finish(
    List<Object> errors,
    Syntax syntax,
    Path typePath,
    TSMap<String, BackSpecData> fields,
    boolean singularRestriction,
    boolean typeRestriction
  ) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    if (type == null) return; // Gaps have null type, take anything
    boolean found = false;
    for (final FreeAtomType child : syntax.getLeafTypes(type)) {
      found = true;
      if (singularRestriction && child.back.size() > 1) {
        errors.add(new AtomCandidatePluralBack(typePath, child, child.back.size()));
      }
      if (typeRestriction && child.back.get(0).isTypedValue()) {
        errors.add(new AtomCandidateTypeNotAllowed(typePath, child));
      }
    }
    if (!found) {
      errors.add(new AtomTypeDoesntExist(typePath, type));
    }
  }

  @Override
  public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
    return new ValueAtom(this, syntax.gap.create());
  }
}
