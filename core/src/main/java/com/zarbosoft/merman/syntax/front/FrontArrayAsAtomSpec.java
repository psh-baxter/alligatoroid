package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomFromArray;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontArrayAsAtomSpec extends FrontSpec {
  public final String field;
  private final Symbol ellipsis;
  private BaseBackArraySpec dataType;

  public FrontArrayAsAtomSpec(Config config) {
    this.field = config.field;
    ellipsis = config.ellipsis;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    return new VisualFrontAtomFromArray(
        context, parent, dataType.get(atom.fields), visualDepth, depthScore, ellipsis);
  }

  @Override
  public void finish(
      MultiError errors, Path typePath, final AtomType atomType, final TSSet<String> middleUsed) {
    middleUsed.add(field);
    dataType = (BaseBackArraySpec) atomType.getDataArray(errors, typePath, field);
  }

  @Override
  public String field() {
    return field;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {}

  public static class Config {
    public final String field;
    public final Symbol ellipsis;

    public Config(String field, Symbol ellipsis) {
      this.field = field;
      this.ellipsis = ellipsis;
    }
  }
}
