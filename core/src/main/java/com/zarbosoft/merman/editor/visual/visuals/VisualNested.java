package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import org.pcollections.PSet;

import java.util.Map;

public abstract class VisualNested extends VisualNestedBase {
  public final ValueAtom value;
  private final ValueAtom.Listener dataListener;

  @Override
  public void dispatch(VisualNestedDispatcher dispatcher) {
    dispatcher.handle(this);
  }

  public VisualNested(
      final Context context,
      final VisualParent parent,
      final ValueAtom value,
      final PSet<Tag> tags,
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    super(tags, visualDepth);
    this.value = value;
    dataListener =
        new ValueAtom.Listener() {
          @Override
          public void set(final Context context, final Atom atom) {
            VisualNested.this.set(context, atom);
          }
        };
    value.addListener(dataListener);
    value.visual = this;
    root(context, parent, alignments, visualDepth, depthScore);
  }

  @Override
  public Atom atomGet() {
    return value.get();
  }

  @Override
  public String nodeType() {
    return value.back().type;
  }

  @Override
  protected Value value() {
    return value;
  }

  @Override
  protected Path getBackPath() {
    return value.getSyntaxPath();
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    value.removeListener(dataListener);
    value.visual = null;
    super.uproot(context, root);
  }

  @Override
  public void tagsChanged(final Context context) {}
}
