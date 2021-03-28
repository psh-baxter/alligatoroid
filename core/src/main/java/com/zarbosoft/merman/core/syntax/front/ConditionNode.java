package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;

public class ConditionNode extends ConditionType {
  public final Is is;

  public static class Config {
    public final Is is;
    public final boolean invert;

    public Config(Is is, boolean invert) {
      this.is = is;
      this.invert = invert;
    }
  }

  public ConditionNode(Config config) {
    super(config.invert);
    this.is = config.is;
  }

  @Override
  public ConditionAttachment create(final Context context, final Atom atom) {
    final boolean show;
    if (atom.valueParentRef == null) {
      show = true;
    } else if (!(atom.type instanceof FreeAtomType)) {
      show = true;
    } else {
      show = AtomType.isPrecedent((FreeAtomType) atom.type, atom.valueParentRef, true);
    }
    final ConditionAttachment condition =
        new ConditionAttachment(invert) {
          @Override
          public void destroy(final Context context) {}
        };
    condition.setState(context, show);
    return condition;
  }

  @Override
  protected boolean defaultOnImplementation() {
    if (is == ConditionNode.Is.PRECEDENT && !invert) return false;
    return true;
  }

  public static enum Is {
    PRECEDENT,
  }
}
