package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.gap.GapCompletionEngine;
import com.zarbosoft.merman.syntax.back.BackSpecData;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;

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

  public abstract boolean selectDown(Context context);

  public abstract Object syntaxLocateStep(String segment);

  public abstract GapCompletionEngine.State createGapEngine();

  public abstract class Parent {

    /**
     * Replace the child with a new atom. (Creates history)
     *
     * @param context
     * @param atom
     */
    public abstract void replace(Context context, Atom atom);

    /**
     * Remove the element if an array. (Creates history)
     *
     * @param context
     */
    public void deleteChild(final Context context) {
      throw new Assertion();
    }

    public abstract String childType();

    public Value value() {
      return Value.this;
    }

    public String id() {
      return back().id;
    }

    public abstract Path path();

    public abstract boolean selectUp(final Context context);

    public abstract Path getSyntaxPath();
  }
}
