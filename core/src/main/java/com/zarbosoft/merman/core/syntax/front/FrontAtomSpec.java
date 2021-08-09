package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtom;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontAtomSpec extends FrontSpec {
  private final String fieldId;
  private final Symbol ellipsis;
  private BaseBackAtomSpec field;

  public FrontAtomSpec(Config config) {
    fieldId = config.fieldId;
    ellipsis = config.ellipsis;
  }

  public BaseBackAtomSpec field() {
    return field;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    return new VisualFieldAtom(
        context, parent, field.get(atom.namedFields), visualDepth, depthScore, ellipsis);
  }

  @Override
  public void finish(
          MultiError errors, SyntaxPath typePath, final AtomType atomType, final TSSet<String> middleUsed) {
    middleUsed.add(fieldId);
    field = atomType.getDataAtom(errors, typePath, fieldId, "front atom spec");
  }

  @Override
  public String fieldId() {
    return fieldId;
  }

    public static class Config {
    public final String fieldId;
    public final Style.Config ellipsisStyle = new Style.Config();
    public Symbol ellipsis = new SymbolTextSpec(new SymbolTextSpec.Config("..."));
    public ROSet<String> tags = ROSet.empty;

    public Config(String fieldId) {
      this.fieldId = fieldId;
    }
  }
}
