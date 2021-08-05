package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class FrontSpec {
  public abstract Visual createVisual(
      Context context, VisualParent parent, Atom atom, int visualDepth, int depthScore);

  public void finish(
      MultiError errors,
      SyntaxPath typePath,
      final AtomType atomType,
      final TSSet<String> middleUsed) {}

  /**
   * Field id or null if non-data front
   *
   * @return
   */
  public abstract String fieldId();
}
