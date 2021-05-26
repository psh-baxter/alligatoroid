package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.visual.Vector;

public class BoxContainer extends StackContainer {
  private final Box box;

  public BoxContainer(Context context, ObboxStyle boxStyle, Container node) {
    super(context);
    add(box = new Box(context));
    addRoot(node);
    box.setStyle(boxStyle);
  }

  @Override
  public void setConverseSpan(Context context, double span) {
    super.setConverseSpan(context, span);
    box.setSize(context, root.converseSpan(), root.transverseSpan());
    box.setPosition(Vector.zero, false);
  }
}
