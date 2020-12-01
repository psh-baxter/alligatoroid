package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.rendaw.common.DeadCode;
import org.pcollections.PSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class FrontSpec {

  public Set<String> tags = new HashSet<>();

  public abstract Visual createVisual(
      Context context,
      VisualParent parent,
      Atom atom,
      PSet<Tag> tags,
      Map<String, Alignment> alignments,
      int visualDepth,
      int depthScore);

  public void finish(final AtomType atomType, final Set<String> middleUsed) {}

  public abstract String field();

  public abstract void dispatch(DispatchHandler handler);

  public abstract static class DispatchHandler {

    public abstract void handle(FrontSymbol front);

    public abstract void handle(FrontArraySpecBase front);

    public abstract void handle(FrontDataAtom front);

    public abstract void handle(FrontPrimitiveSpec front);

    public abstract void handle(FrontGapBase front);
  }

  public abstract static class NodeDispatchHandler extends DispatchHandler {

    @Override
    public final void handle(final FrontSymbol front) {}

    @Override
    public final void handle(final FrontPrimitiveSpec front) {}

    @Override
    public final void handle(final FrontGapBase front) {}
  }

  public abstract static class NodeOnlyDispatchHandler extends DispatchHandler {

    public final void handle(final FrontSymbol front) {
      throw new DeadCode();
    }

    public final void handle(final FrontPrimitiveSpec front) {
      throw new DeadCode();
    }

    @Override
    public final void handle(final FrontGapBase front) {
      throw new DeadCode();
    }
  }

  public abstract static class DataDispatchHandler extends DispatchHandler {
    @Override
    public void handle(final FrontSymbol front) {}
  }
}
