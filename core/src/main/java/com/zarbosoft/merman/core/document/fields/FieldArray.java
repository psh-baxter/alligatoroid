package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomFromArray;
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

  /**
   * Initializes the field without creating history.  Use only if none of the subtrees have any history (ex:
   * initial document load, paste) otherwise if the atom creation is undone then redone, the atoms will have the
   * wrong parents.
   * @param data
   */
  public void initialSet(TSList<Atom> data) {
    this.data.addAll(data);
    for (Atom v : data) {
      v.setFieldParentRef(new Parent(this));
    }
    renumber(0);
  }

  public void renumber(final int from) {
    int sum;
    if (from == 0) {
      sum = 0;
    } else {
      final Atom prior = data.get(from - 1);
      final Parent parent = (Parent) prior.fieldParentRef;
      sum = parent.backIndex + prior.type.back().size();
    }
    for (int i = from; i < data.size(); ++i) {
      Atom atom = data.get(i);
      final Parent parent = ((Parent) atom.fieldParentRef);
      parent.index = i;
      parent.backIndex = sum;
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
    if (data.isEmpty() && !context.cursorFactory.prepSelectEmptyArray(context, this)) {
      return false;
    }
    if (context.window) context.windowAdjustMinimalTo(this);
    if (visual instanceof VisualFieldArray)
      ((VisualFieldArray) visual).select(context, leadFirst, start, end);
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

  public static class Parent extends Field.Parent<FieldArray> {
    public int index = 0;
    public int backIndex = 0;

    public Parent(FieldArray value) {
      super(value);
    }

    @Override
    public String valueType() {
      return field.back.elementAtomType();
    }

    @Override
    public SyntaxPath path() {
      return field.back.getPath(field, backIndex);
    }

    @Override
    public boolean selectValue(final Context context) {
      field.selectInto(context, true, index, index);
      return true;
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      SyntaxPath out;
      if (field.atomParentRef == null) out = new SyntaxPath();
      else out = field.atomParentRef.getSyntaxPath();
      return out.add(Integer.toString(index));
    }

    @Override
    public void dispatch(ParentDispatcher dispatcher) {
      dispatcher.handle(this);
    }
  }
}
