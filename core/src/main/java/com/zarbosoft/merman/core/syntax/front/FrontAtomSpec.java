package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.VisualParent;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontAtom;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontAtomSpec extends FrontSpec {
  private final String back;
  private final Symbol ellipsis;
  private BaseBackAtomSpec dataType;

  public FrontAtomSpec(Config config) {
    back = config.back;
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
    middleUsed.add(back);
    dataType = atomType.getDataAtom(errors, typePath, back);
  }

  @Override
  public String field() {
    return back;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public static class Config {
    public final String back;
    public final Style.Config ellipsisStyle = new Style.Config();
    public Symbol ellipsis = new SymbolTextSpec(new SymbolTextSpec.Config("..."));
    public ROSet<String> tags = ROSet.empty;

    public Config(String back) {
      this.back = back;
    }
  }
}
