package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.display.derived.CourseGroup;

import java.util.ArrayList;
import java.util.List;

public class RowLayout {
  public final CourseGroup group;
  List<DisplayNode> nodes = new ArrayList<>();

  public RowLayout(final Context context) {
    this(context.display);
  }

  public RowLayout(final Display display) {
    this.group = new CourseGroup(display.group());
  }

  public void add(final CourseDisplayNode node) {
    group.add(node);
    nodes.add(node);
  }

  public void layout() {
    double converse = 0;
    double maxAscent = 0;
    for (final DisplayNode node : nodes) {
        maxAscent = Math.max(maxAscent, ((CourseDisplayNode) node).ascent());
    }
    group.setBaselineTransverse(maxAscent);
    for (final DisplayNode node : nodes) {
      node.setConverse(converse, false);
      converse += node.converseSpan();
    }
  }
}
