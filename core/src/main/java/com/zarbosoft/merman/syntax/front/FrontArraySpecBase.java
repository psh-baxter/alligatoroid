package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class FrontArraySpecBase extends FrontSpec {

  public List<FrontSymbol> prefix = new ArrayList<>();

  public List<FrontSymbol> suffix = new ArrayList<>();

  public List<FrontSymbol> separator = new ArrayList<>();

  public boolean tagFirst = false;

  public boolean tagLast = false;

  public Symbol ellipsis = new SymbolTextSpec("...");

  public BaseBackArraySpec dataType;

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final PSet<Tag> tags,
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    return new VisualArray(
        context,
        parent,
        dataType.get(atom.fields),
        tags.plus(new PartTag("array"))
            .plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
        alignments,
        visualDepth,
        depthScore) {

      @Override
      protected boolean tagLast() {
        return tagLast;
      }

      @Override
      protected boolean tagFirst() {
        return tagFirst;
      }

      @Override
      protected List<FrontSymbol> getPrefix() {
        return prefix;
      }

      @Override
      protected List<FrontSymbol> getSeparator() {
        return separator;
      }

      @Override
      protected List<FrontSymbol> getSuffix() {
        return suffix;
      }

      @Override
      protected Symbol ellipsis() {
        return ellipsis;
      }
    };
  }

  @Override
  public void finish(
    List<Object> errors, Path typePath, final AtomType atomType, final Set<String> middleUsed
  ) {
    middleUsed.add(field());
    ((BaseBackArraySpec) atomType.fields.get(field())).front = this;
    dataType = atomType.getDataArray(errors,typePath, field());
  }

  public abstract String field();

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }
}
