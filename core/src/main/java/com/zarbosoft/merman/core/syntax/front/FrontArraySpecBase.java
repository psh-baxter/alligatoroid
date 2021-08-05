package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class FrontArraySpecBase extends FrontSpec {
  public final ROList<FrontSymbolSpec> prefix;
  public final ROList<FrontSymbolSpec> suffix;
  public final ROList<FrontSymbolSpec> separator;
  public final Symbol ellipsis;
  public final Symbol empty;
  public BaseBackArraySpec field;

  public FrontArraySpecBase(Config config) {
    this.prefix = config.prefix;
    this.suffix = config.suffix;
    this.separator = config.separator;
    this.ellipsis = config.ellipsis;
    empty = config.empty;
  }

  public BaseBackArraySpec field() {
    return field;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    VisualFieldArray out = new VisualFieldArray(this, parent, atom, visualDepth);
    out.root(context, parent, depthScore, depthScore);
    return out;
  }

  @Override
  public void finish(
      MultiError errors,
      SyntaxPath typePath,
      final AtomType atomType,
      final TSSet<String> middleUsed) {
    middleUsed.add(fieldId());
    field = atomType.getDataArray(errors, typePath, fieldId(), "front array spec");
  }

  public abstract String fieldId();

    public static class Config {
    public ROSet<String> tags = ROSet.empty;
    public ROList<FrontSymbolSpec> prefix = ROList.empty;
    public ROList<FrontSymbolSpec> suffix = ROList.empty;
    public ROList<FrontSymbolSpec> separator = ROList.empty;
    public Symbol ellipsis = new SymbolTextSpec(new SymbolTextSpec.Config("..."));
    public Symbol empty = new SymbolSpaceSpec(new SymbolSpaceSpec.Config());

    public Config() {}

    public Config prefix(ROList<FrontSymbolSpec> prefix) {
      this.prefix = prefix;
      return this;
    }

    public Config suffix(ROList<FrontSymbolSpec> suffix) {
      this.suffix = suffix;
      return this;
    }

    public Config separator(ROList<FrontSymbolSpec> separator) {
      this.separator = separator;
      return this;
    }
  }
}
