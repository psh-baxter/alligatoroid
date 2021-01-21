package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.misc.ROSetRef;
import com.zarbosoft.merman.syntax.AtomType;

import java.util.Set;

public abstract class FrontSpec {

  public final ROSet<String> tags;

  protected FrontSpec(ROSet<String> tags) {
    this.tags = tags;
  }

  public abstract Visual createVisual(
          Context context,
          VisualParent parent,
          Atom atom,
          ROMap<String, Alignment> alignments,
          int visualDepth,
          int depthScore);

  public void finish(
      MultiError errors, Path typePath, final AtomType atomType, final Set<String> middleUsed) {}

  public abstract String field();

  public abstract void dispatch(DispatchHandler handler);

  public abstract static class DispatchHandler {

    public abstract void handle(FrontSymbol front);

    public abstract void handle(FrontArraySpecBase front);

    public abstract void handle(FrontAtomSpec front);

    public abstract void handle(FrontPrimitiveSpec front);
  }
}
