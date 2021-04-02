package com.zarbosoft.merman.core.document.values;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontAtomFromArray;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.HashSet;
import java.util.Set;

public class FieldArray extends Field {
  public final TSList<Atom> data = new TSList<>();
  public final Set<Listener> listeners = new HashSet<>();
  private final BaseBackArraySpec back;
  public Visual visual = null;

  public FieldArray(final BaseBackArraySpec back) {
    this.back = back;
  }

  public void initialSet(TSList<Atom> data) {
    this.data.addAll(data);
    for (Atom v : data) {
      v.setValueParentRef(new ArrayParent(this));
    }
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
    for (int i = from; i < data.size(); ++i) {
      Atom atom = data.get(i);
      final ArrayParent parent = ((ArrayParent) atom.valueParentRef);
      parent.index = i;
      parent.actualIndex = sum;
      sum += atom.type.back().size();
    }
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

  public interface Listener {

    void changed(Context context, int index, int remove, ROList<Atom> add);
  }

  public static class ArrayParent extends Parent<FieldArray> {
    public int index = 0;
    public int actualIndex = 0;

    public ArrayParent(FieldArray value) {
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
