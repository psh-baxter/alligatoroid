package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackSpecData;

public abstract class Value {
  public Atom.Parent parent = null;

  public void setParent(final Atom.Parent parent) {
    this.parent = parent;
  }

  public abstract BackSpecData back();

  public final Path getSyntaxPath() {
    if (parent == null) return new Path();
    else return parent.getSyntaxPath();
  }

  public abstract boolean selectInto(Context context);

  public abstract Object syntaxLocateStep(String segment);

  public abstract static class Parent<T extends Value> {
    public final T child;

    protected Parent(T child) {
      this.child = child;
    }

    public abstract String childType();

    public String id() {
      return child.back().id;
    }

    public abstract Path path();

    public abstract boolean selectChild(final Context context);

    public abstract Path getSyntaxPath();

    public abstract void dispatch(ParentDispatcher dispatcher);
  }

  public interface ParentDispatcher {
    void handle(ValueArray.ArrayParent parent);

    void handle(ValueAtom.NodeParent parent);
  }
}
