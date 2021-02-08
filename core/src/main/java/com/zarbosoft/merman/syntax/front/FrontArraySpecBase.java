package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class FrontArraySpecBase extends FrontSpec {
  public final ROList<FrontSymbol> prefix;
  public final ROList<FrontSymbol> suffix;
  public final ROList<FrontSymbol> separator;
  public final boolean tagFirst;
  public final boolean tagLast;
  public final Symbol ellipsis;
  private BaseBackArraySpec dataType;

  public FrontArraySpecBase(Config config) {
    super(config.tags);
    this.prefix = config.prefix;
    this.suffix = config.suffix;
    this.separator = config.separator;
    this.tagFirst = config.tagFirst;
    this.tagLast = config.tagLast;
    this.ellipsis = config.ellipsis;
  }

  public BaseBackArraySpec dataType() {
    return dataType;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    LocalVisualFrontArray out = new LocalVisualFrontArray(this, context, parent, atom, visualDepth, depthScore);
    out.root(context, parent, depthScore, depthScore);
    return out;
  }

  @Override
  public void finish(
      MultiError errors, Path typePath, final AtomType atomType, final TSSet<String> middleUsed) {
    middleUsed.add(field());
    dataType = atomType.getDataArray(errors, typePath, field());
  }

  public abstract String field();

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public static class Config {
    public ROSet<String> tags = ROSet.empty;
    public ROList<FrontSymbol> prefix = ROList.empty;
    public ROList<FrontSymbol> suffix = ROList.empty;
    public ROList<FrontSymbol> separator = ROList.empty;
    public boolean tagFirst = false;
    public boolean tagLast = false;
    public Symbol ellipsis = new SymbolTextSpec("...");

    public Config() {}

    public Config(
        ROSet<String> tags,
        ROList<FrontSymbol> prefix,
        ROList<FrontSymbol> suffix,
        ROList<FrontSymbol> separator,
        boolean tagFirst,
        boolean tagLast,
        Symbol ellipsis) {
      this.tags = tags;
      this.prefix = prefix;
      this.suffix = suffix;
      this.separator = separator;
      this.tagFirst = tagFirst;
      this.tagLast = tagLast;
      this.ellipsis = ellipsis;
    }
  }

  private static class LocalVisualFrontArray extends VisualFrontArray {
    private final FrontArraySpecBase frontArraySpecBase;

    public LocalVisualFrontArray(
        FrontArraySpecBase frontArraySpecBase,
        Context context,
        VisualParent parent,
        Atom atom,
        int visualDepth,
        int depthScore) {
      super(parent, frontArraySpecBase.dataType.get(atom.fields), visualDepth);
      this.frontArraySpecBase = frontArraySpecBase;
    }

    @Override
    protected boolean tagLast() {
      return frontArraySpecBase.tagLast;
    }

    @Override
    protected boolean tagFirst() {
      return frontArraySpecBase.tagFirst;
    }

    @Override
    protected ROList<FrontSymbol> getElementPrefix() {
      return frontArraySpecBase.prefix;
    }

    @Override
    protected ROList<FrontSymbol> getSeparator() {
      return frontArraySpecBase.separator;
    }

    @Override
    protected ROList<FrontSymbol> getElementSuffix() {
      return frontArraySpecBase.suffix;
    }

    @Override
    protected Symbol ellipsis() {
      return frontArraySpecBase.ellipsis;
    }
  }
}
