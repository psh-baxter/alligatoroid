package com.zarbosoft.merman.syntax.front;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;

import java.util.Set;

public class FrontPrimitiveSpec extends FrontSpec {
  public final String field;
  public BaseBackPrimitiveSpec dataType;

  public static class Config {
    final String field;
    final ROSet<String> tags;

    public Config(String field, ROSet<String> tags) {
      this.field = field;
      this.tags = tags;
    }
  }

  public FrontPrimitiveSpec(Config config) {
    super(config.tags);
    field = config.field;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final ROMap<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    return new VisualFrontPrimitive(context, parent,this, dataType.get(atom.fields), visualDepth);
  }

  @Override
  public void finish(
      MultiError errors, Path typePath, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(field);
    this.dataType = atomType.getDataPrimitive(errors, typePath, field);
  }

  @Override
  public String field() {
    return field;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public Set<String> tags() {
    return ImmutableSet.copyOf(tags);
  }
}
