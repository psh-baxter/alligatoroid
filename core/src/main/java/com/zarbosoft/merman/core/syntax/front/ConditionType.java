package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.condition.ConditionAttachment;

public abstract class ConditionType {
  public final boolean invert;

  protected ConditionType(boolean invert) {
    this.invert = invert;
  }

  public abstract ConditionAttachment create(Context context, Atom atom);

  public boolean defaultOn() {
    return invert ? defaultOnImplementation() : !defaultOnImplementation();
  }

  protected abstract boolean defaultOnImplementation();

    public abstract void finish(MultiError errors, SyntaxPath typePath, AtomType atomType);
}
