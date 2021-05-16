package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.rendaw.common.ROList;

public class VisualFieldAtomFromArray extends VisualFieldAtomBase {
  public final FieldArray value;
  private final FieldArray.Listener dataListener;

  public VisualFieldAtomFromArray(
      final Context context,
      final VisualParent parent,
      final FieldArray value,
      final int visualDepth,
      final int depthScore,
      Symbol ellipsis) {
    super(visualDepth, ellipsis);
    this.value = value;
    dataListener =
        new FieldArray.Listener() {
          @Override
          public void changed(
              final Context context, final int index, final int remove, final ROList<Atom> add) {
            set(context, add.get(0));
          }
        };
    value.addListener(dataListener);
    value.visual = this;
    root(context, parent, visualDepth, depthScore);
  }

  @Override
  public String nodeType() {
    return value.back().elementAtomType();
  }

  @Override
  public void dispatch(VisualNestedDispatcher dispatcher) {
    dispatcher.handle(this);
  }

  @Override
  public Atom atomGet() {
    if (value.data.isEmpty()) return null;
    return value.data.get(0);
  }

  @Override
  protected Field value() {
    return value;
  }

  @Override
  public String backId() {
    return value.back().id;
  }

  @Override
  protected SyntaxPath getBackPath() {
    return value.getSyntaxPath();
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    value.removeListener(dataListener);
    value.visual = null;
    super.uproot(context, root);
  }
}
