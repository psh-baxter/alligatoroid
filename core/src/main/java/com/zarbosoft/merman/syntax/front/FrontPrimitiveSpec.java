package com.zarbosoft.merman.syntax.front;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FrontPrimitiveSpec extends FrontSpec {

  public String field;
  private Set<String> tags = new HashSet<>();
  private BaseBackPrimitiveSpec dataType;

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final PSet<Tag> tags,
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    return new VisualPrimitive(
        context,
        parent,
        dataType.get(atom.fields),
        HashTreePSet.from(tags)
            .plus(new PartTag("primitive"))
            .plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
        visualDepth,
        depthScore);
  }

  @Override
  public void finish(final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(field);
    this.dataType = atomType.getDataPrimitive(field);
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

  public void tags(final Set<String> tags) {
    if (!this.tags.isEmpty()) throw new AssertionError();
    this.tags = tags;
  }
}
