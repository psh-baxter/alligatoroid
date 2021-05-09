package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtom;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;

import java.util.HashSet;
import java.util.Set;

public class FieldAtom extends Field {
  public static final String SYNTAX_PATH_KEY = "atom";
  public final Set<Listener> listeners = new HashSet<>();
  private final BaseBackAtomSpec back;
  public VisualFrontAtom visual;
  public Atom data; // INVARIANT: Never null when in tree

  public FieldAtom(final BaseBackAtomSpec back) {
    this.back = back;
  }

  /**
   * Initializes the field without creating history.  Use only if none of the subtrees have any history (ex:
   * initial document load, paste) otherwise if the atom creation is undone then redone, the atoms will have the
   * wrong parents.
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
    select(context);
    return true;
  }

  @Override
  public Object syntaxLocateStep(String segment) {
    if (!SYNTAX_PATH_KEY.equals(segment)) return null;
    return data;
  }

  public void select(final Context context) {
    if (context.window) context.windowAdjustMinimalTo(this);
    visual.select(context);
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
    public boolean selectValue(final Context context) {
      field.select(context);
      return true;
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      SyntaxPath out;
      if (field.atomParentRef == null) out = new SyntaxPath();
      else out = field.atomParentRef.getSyntaxPath();
      return out.add(SYNTAX_PATH_KEY);
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
