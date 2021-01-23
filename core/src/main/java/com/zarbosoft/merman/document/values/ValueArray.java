package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomFromArray;
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
              v.setValueParentRef(new ArrayParent(this));
            });
    renumber(0);
  }

  public void renumber(final int from) {
    int sum;
    if (from == 0) {
      sum = 0;
    } else {
      final Atom prior = data.get(from - 1);
      final ArrayParent parent = (ArrayParent) prior.valueParentRef;
      sum = parent.actualIndex + prior.type.back().size();
    }
    for (final Pair<Integer, Atom> p : iterable(enumerate(data.stream().skip(from), from))) {
      final ArrayParent parent = ((ArrayParent) p.second.valueParentRef);
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
  public boolean selectInto(final Context context) {
    return selectInto(context, true, 0, 0);
  }

  public boolean selectInto(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (data.isEmpty()) {
      return false;
    }
    if (context.window) context.windowAdjustMinimalTo(this);
    if (visual instanceof VisualFrontArray)
      ((VisualFrontArray) visual).select(context, leadFirst, start, end);
    else if (visual instanceof VisualFrontAtomFromArray)
      ((VisualFrontAtomFromArray) visual).select(context);
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
    if (atomParentRef.atom().valueParentRef != null) throw new AssertionError();
    data.add(value);
    value.setValueParentRef(new ArrayParent(this));
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
    public String valueType() {
      return value.back.elementAtomType();
    }

    @Override
    public Path path() {
      return value.back.getPath(value, actualIndex);
    }

    @Override
    public boolean selectValue(final Context context) {
      value.selectInto(context, true, index, index);
      return true;
    }

    @Override
    public Path getSyntaxPath() {
      Path out;
      if (value.atomParentRef == null) out = new Path();
      else out = value.atomParentRef.getSyntaxPath();
      return out.add(Integer.toString(index));
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
