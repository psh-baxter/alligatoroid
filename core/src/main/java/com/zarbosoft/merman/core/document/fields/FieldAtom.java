package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtom;

import java.util.HashSet;
import java.util.Set;

public class FieldAtom extends Field {
  public final Set<Listener> listeners = new HashSet<>();
  private final BaseBackAtomSpec back;
  public VisualFieldAtom visual;
  public Atom data; // INVARIANT: Never null when in tree

  public FieldAtom(final BaseBackAtomSpec back) {
    this.back = back;
  }

  /**
   * Initializes the field without creating history. Use only if none of the subtrees have any
   * history (ex: initial document load, paste) otherwise if the atom creation is undone then
   * redone, the atoms will have the wrong parents.
   *
   * @param data
   */
  public void initialSet(Atom data) {
    this.data = data;
    if (data != null) data.setFieldParentRef(new Parent(this));
  }

  public void addListener(final Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(final Listener listener) {
    listeners.remove(listener);
  }

  public Atom get() {
    return data;
  }

  @Override
  public BaseBackAtomSpec back() {
    return back;
  }

  @Override
  public boolean selectInto(final Context context) {
    if (context.window) context.windowAdjustMinimalTo(this);
    atomParentRef.atom().visual.selectById(context, back.id);
    return true;
  }

  @Override
  public Object syntaxLocateStep(String segment) {
    return data.syntaxLocateStep(segment);
  }

  public abstract static class Listener {
    public abstract void set(Context context, Atom atom);
  }

  public static class Parent extends Field.Parent<FieldAtom> {
    public Parent(FieldAtom child) {
      super(child);
    }

    @Override
    public String valueType() {
      return field.back.type;
    }

    @Override
    public String id() {
      return field.back.id;
    }

    @Override
    public SyntaxPath path() {
      return field.getSyntaxPath();
    }

    @Override
    public boolean selectField(final Context context) {
      field.atomParentRef.atom().visual.selectById(context, field.back.id);
      return true;
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return field.getSyntaxPath();
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
