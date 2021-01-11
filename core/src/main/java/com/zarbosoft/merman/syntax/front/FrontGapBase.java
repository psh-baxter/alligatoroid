package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.gap.TwoColumnChoice;
import com.zarbosoft.merman.editor.gap.GapVisualPrimitive;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FrontGapBase extends FrontSpec {
  public BaseBackPrimitiveSpec dataType;

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final PSet<Tag> tags,
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    return new GapVisualPrimitive(this, context, parent, atom, tags, visualDepth, depthScore);
  }

  @Override
  public void finish(
      List<Object> errors, Path typePath, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(field());
    this.dataType = atomType.getDataPrimitive(errors, typePath, field());
  }

  @Override
  public String field() {
    return "gap";
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public abstract void deselect(
      Context context, Atom self, String string);
}
