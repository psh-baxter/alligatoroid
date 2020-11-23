package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

public abstract class BackSpec {
  public Parent parent = null;

  public abstract Node buildBackRule(Syntax syntax, AtomType atomType);

  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {}

  public final Pair<Integer, Path> getSubpath() {
    if (parent instanceof AtomType.NodeBackParent)
      return new Pair<>(((AtomType.NodeBackParent) parent).index, new Path());
    else if (parent instanceof PartParent) {
      final Pair<Integer, Path> base = ((PartParent) parent).part().getSubpath();
      final String section = ((PartParent) parent).pathSection();
      return new Pair<>(base.first, section == null ? base.second : base.second.add(section));
    } else throw new DeadCode();
  }

  public abstract static class Parent {}

  public abstract static class PartParent extends Parent {
    public abstract BackSpec part();

    public abstract String pathSection();
  }
}
