package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtom;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;

import java.util.HashSet;
import java.util.Set;

public class ValueAtom extends Value {
  public static final String SYNTAX_PATH_KEY = "atom";
  public final Set<Listener> listeners = new HashSet<>();
  private final BaseBackAtomSpec back;
  public VisualFrontAtom visual;
  public Atom data; // INVARIANT: Never null when in tree

  public ValueAtom(final BaseBackAtomSpec back, final Atom data) {
    this.back = back;
    this.data = data;
    if (data != null) data.setParent(new NodeParent(this));
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
    if (context.window) {
      if (visual == null || data.visual == null) {
        context.createWindowForSelection(this, context.ellipsizeThreshold);
      }
    }
    visual.select(context);
  }

  public abstract static class Listener {
    public abstract void set(Context context, Atom atom);
  }

  public static class NodeParent extends Parent<ValueAtom> {
    public NodeParent(ValueAtom child) {
      super(child);
    }

    @Override
    public String childType() {
      return child.back.type;
    }

    @Override
    public String id() {
      return child.back.id;
    }

    @Override
    public Path path() {
      return child.getSyntaxPath();
    }

    @Override
    public boolean selectChild(final Context context) {
      child.select(context);
      return true;
    }

    @Override
    public Path getSyntaxPath() {
      Path out;
      if (child.parent == null) out = new Path();
      else out = child.parent.getSyntaxPath();
      return out.add(SYNTAX_PATH_KEY);
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
