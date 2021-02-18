package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.ROList;

public class VisualFrontAtomFromArray extends VisualFrontAtomBase {
  public final ValueArray value;
  private final ValueArray.Listener dataListener;

  public VisualFrontAtomFromArray(
      final Context context,
      final VisualParent parent,
      final ValueArray value,
      final int visualDepth,
      final int depthScore,
      Symbol ellipsis) {
    super(visualDepth, ellipsis);
    this.value = value;
    dataListener =
        new ValueArray.Listener() {
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
  protected Value value() {
    return value;
  }

  @Override
  protected Path getBackPath() {
    return value.getSyntaxPath();
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    value.removeListener(dataListener);
    value.visual = null;
    super.uproot(context, root);
  }
}
