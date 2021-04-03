package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.rendaw.common.Assertion;

public abstract class Field {
  public Atom.Parent atomParentRef = null;

  public void setAtomParentRef(final Atom.Parent atomParentRef) {
    if (this.atomParentRef != null) {
      throw new Assertion();
    }
    this.atomParentRef = atomParentRef;
  }

  public abstract BackSpecData back();

  public final SyntaxPath getSyntaxPath() {
    if (atomParentRef == null) return new SyntaxPath();
    else return atomParentRef.getSyntaxPath();
  }

  public abstract boolean selectInto(Context context);

  public abstract Object syntaxLocateStep(String segment);

  public interface ParentDispatcher {
    void handle(FieldArray.ArrayParent parent);

    void handle(FieldAtom.NodeParent parent);
  }

  public abstract static class Parent<T extends Field> {
    public final T value;

    protected Parent(T value) {
      this.value = value;
    }

    public abstract String valueType();

    public String id() {
      return value.back().id;
    }

    public abstract SyntaxPath path();

    public abstract boolean selectValue(final Context context);

    public abstract SyntaxPath getSyntaxPath();

    public abstract void dispatch(ParentDispatcher dispatcher);
  }
}
