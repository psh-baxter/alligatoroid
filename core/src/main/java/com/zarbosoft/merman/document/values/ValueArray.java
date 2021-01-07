package com.zarbosoft.merman.document.values;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
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
              v.setParent(new ArrayParent());
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
    select(context, true, 0, 0);
    return true;
  }

  public void select(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (data.isEmpty()) {
      final Atom initial = createAndAddDefault(context, 0);
      if (initial.visual.selectDown(context)) return;
    }
    if (context.window) {
      final Atom firstChild = data.get(start);
      if (visual == null || firstChild.visual == null) {
        context.createWindowForSelection(this, context.syntax.ellipsizeThreshold);
      }
    }
    if (visual instanceof VisualArray) ((VisualArray) visual).select(context, true, start, end);
    else if (visual instanceof VisualNestedFromArray)
      ((VisualNestedFromArray) visual).select(context);
    else throw new DeadCode();
  }

  public Atom createAndAddDefault(final Context context, final int index) {
    final List<FreeAtomType> childTypes = context.syntax.getLeafTypes(back.elementAtomType());
    final Atom element;
    if (childTypes.size() == 1) element = childTypes.get(0).create(context.syntax);
    else element = context.syntax.gap.create();
    context.history.apply(context, new ChangeArray(this, index, 0, ImmutableList.of(element)));
    return element;
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
    value.setParent(new ArrayParent());
    renumber(0);
  }

  public interface Listener {

    void changed(Context context, int index, int remove, List<Atom> add);
  }

  public class ArrayParent extends Parent {
    public int index = 0;
    public int actualIndex = 0;

    @Override
    public void replace(final Context context, final Atom atom) {
      final int index = this.index;
      context.history.apply(
          context, new ChangeArray(ValueArray.this, index, 1, ImmutableList.of(atom)));
    }

    @Override
    public void deleteChild(final Context context) {
      context.history.apply(
          context, new ChangeArray(ValueArray.this, index, 1, ImmutableList.of()));
    }

    @Override
    public String childType() {
      return back.elementAtomType();
    }

    @Override
    public Path path() {
      return back.getPath(ValueArray.this, actualIndex);
    }

    @Override
    public boolean selectUp(final Context context) {
      select(context, true, index, index);
      return true;
    }

    @Override
    public Path getSyntaxPath() {
      Path out;
      if (parent == null) out = new Path();
      else out = parent.getSyntaxPath();
      return out.add(Integer.toString(index));
    }
  }
}
