package com.zarbosoft.merman.document.values;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedFromArray;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;

public class ValueArray extends Value {
  public final List<Atom> data = new ArrayList<>();
  public final Set<Listener> listeners = new HashSet<>();
  private final BaseBackArraySpec back;
  public Visual visual = null;

  public ValueArray(final BaseBackArraySpec back, final List<Atom> data) {
    this.back = back;
    this.data.addAll(data);
    data.stream()
        .forEach(
            v -> {
              v.setParent(new ArrayParent(this));
            });
    renumber(0);
  }

  public void renumber(final int from) {
    int sum;
    if (from == 0) {
      sum = 0;
    } else {
      final Atom prior = data.get(from - 1);
      final ArrayParent parent = (ArrayParent) prior.parent;
      sum = parent.actualIndex + prior.type.back().size();
    }
    for (final Pair<Integer, Atom> p : iterable(enumerate(data.stream().skip(from), from))) {
      final ArrayParent parent = ((ArrayParent) p.second.parent);
      parent.index = p.first;
      parent.actualIndex = sum;
      sum += p.second.type.back().size();
    }
  }

  public ValueArray(final BaseBackArraySpec back) {
    this.back = back;
  }

  public BaseBackArraySpec back() {
    return back;
  }

  @Override
  public boolean selectDown(final Context context) {
    return select(context, true, 0, 0);
  }

  public boolean select(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (data.isEmpty()) {
      if (context.createArrayDefault == null) return false;
      final Atom initial = context.createArrayDefault.create(context, this);
      initial.visual.selectDown(context);
    }
    if (context.window) {
      final Atom firstChild = data.get(start);
      if (visual == null || firstChild.visual == null) {
        context.createWindowForSelection(this, context.syntax.ellipsizeThreshold);
      }
    }
    if (visual instanceof VisualArray)
      ((VisualArray) visual).select(context, leadFirst, start, end);
    else if (visual instanceof VisualNestedFromArray)
      ((VisualNestedFromArray) visual).select(context);
    else throw new DeadCode();
    return true;
  }

  @Override
  public Object syntaxLocateStep(String segment) {
    int val;
    try {
      val = Integer.parseInt(segment);
    } catch (IllegalArgumentException e) {
      return null;
    }
    if (val < 0 || val >= data.size()) return null;
    return data.get(val);
  }

  public void addListener(final Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(final Listener listener) {
    listeners.remove(listener);
  }

  public void sideload(final Atom value) {
    if (!data.isEmpty()) throw new AssertionError();
    if (parent.atom().parent != null) throw new AssertionError();
    data.add(value);
    value.setParent(new ArrayParent(this));
    renumber(0);
  }

  public interface Listener {

    void changed(Context context, int index, int remove, List<Atom> add);
  }

  public static class ArrayParent extends Parent<ValueArray> {
    public int index = 0;
    public int actualIndex = 0;

    public ArrayParent(ValueArray value) {
      super(value);
    }

    @Override
    public String childType() {
      return value.back.elementAtomType();
    }

    @Override
    public Path path() {
      return value.back.getPath(value, actualIndex);
    }

    @Override
    public boolean selectUp(final Context context) {
      value.select(context, true, index, index);
      return true;
    }

    @Override
    public Path getSyntaxPath() {
      Path out;
      if (value.parent == null) out = new Path();
      else out = value.parent.getSyntaxPath();
      return out.add(Integer.toString(index));
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
