package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.CourseDisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import elemental2.dom.HTMLElement;

public class JSCourseDisplayNode extends JSDisplayNode implements CourseDisplayNode {
  protected int ascent;
  protected int descent;

  protected JSCourseDisplayNode(JSDisplay display, HTMLElement element) {
    super(display, element);
  }

  @Override
  public final int ascent() {
    return ascent;
  }

  @Override
  public final int descent() {
    return descent;
  }

  @Override
  public int baselineTransverse() {
    return transverse + ascent;
  }

  @Override
  public final void setBaselineTransverse(int baseline, boolean animate) {
    setTransverse(baseline - ascent, animate);
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    setPosition(new Vector(vector.converse, vector.transverse - ascent), animate);
  }

  @Override
  public int transverseSpan() {
    return ascent + descent;
  }
}
