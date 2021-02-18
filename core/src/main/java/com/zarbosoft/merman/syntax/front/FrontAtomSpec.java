package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtom;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontAtomSpec extends FrontSpec {
  private final String field;
  private final Symbol ellipsis;
  private BaseBackAtomSpec dataType;

  public FrontAtomSpec(Config config) {
    field = config.middle;
    ellipsis = config.ellipsis;
  }

  public BaseBackAtomSpec getDataType() {
    return dataType;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    return new VisualFrontAtom(
        context, parent, dataType.get(atom.fields), visualDepth, depthScore, ellipsis);
  }

  @Override
  public void finish(
      MultiError errors, Path typePath, final AtomType atomType, final TSSet<String> middleUsed) {
    middleUsed.add(field);
    dataType = atomType.getDataAtom(errors, typePath, field);
  }

  @Override
  public String field() {
    return field;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public static class Config {
    public final String middle;
    public final Style.Config ellipsisStyle = new Style.Config();
    public Symbol ellipsis = new SymbolTextSpec(new SymbolTextSpec.Config("..."));
    public ROSet<String> tags = ROSet.empty;

    public Config(String middle) {
      this.middle = middle;
    }
  }
}
