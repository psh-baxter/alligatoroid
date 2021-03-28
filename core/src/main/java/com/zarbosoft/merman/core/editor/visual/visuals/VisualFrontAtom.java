package com.zarbosoft.merman.core.editor.visual.visuals;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.document.values.FieldAtom;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.VisualParent;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;

public class VisualFrontAtom extends VisualFrontAtomBase {
  public final FieldAtom value;
  private final FieldAtom.Listener dataListener;

  @Override
  public void dispatch(VisualNestedDispatcher dispatcher) {
    dispatcher.handle(this);
  }

  public VisualFrontAtom(
          final Context context,
          final VisualParent parent,
          final FieldAtom value,
          final int visualDepth,
          final int depthScore, Symbol ellipsis) {
    super(visualDepth, ellipsis);
    this.value = value;
    dataListener =
        new FieldAtom.Listener() {
          @Override
          public void set(final Context context, final Atom atom) {
            VisualFrontAtom.this.set(context, atom);
          }
        };
    value.addListener(dataListener);
    value.visual = this;
    root(context, parent, visualDepth, depthScore);
  }

  @Override
  public Atom atomGet() {
    return value.get();
  }

  @Override
  public String nodeType() {
    return value.back().type;
  }

  @Override
  protected Field value() {
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
