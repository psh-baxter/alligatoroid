package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackSpecData;

public abstract class Value {
  public Atom.Parent atomParentRef = null;

  public void setAtomParentRef(final Atom.Parent atomParentRef) {
    this.atomParentRef = atomParentRef;
  }

  public abstract BackSpecData back();

  public final Path getSyntaxPath() {
    if (atomParentRef == null) return new Path();
    else return atomParentRef.getSyntaxPath();
  }

  public abstract boolean selectInto(Context context);

  public abstract Object syntaxLocateStep(String segment);

  public abstract static class Parent<T extends Value> {
    public final T value;

    protected Parent(T value) {
      this.value = value;
    }

    public abstract String valueType();

    public String id() {
      return value.back().id;
    }

    public abstract Path path();

    public abstract boolean selectValue(final Context context);

    public abstract Path getSyntaxPath();

    public abstract void dispatch(ParentDispatcher dispatcher);
  }

  public interface ParentDispatcher {
    void handle(ValueArray.ArrayParent parent);

    void handle(ValueAtom.NodeParent parent);
  }
}
