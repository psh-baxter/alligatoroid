package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.condition.ConditionAttachment;

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
}
