package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.editor.display.CourseDisplayNode;
import com.zarbosoft.merman.core.editor.visual.Vector;
import elemental2.dom.HTMLElement;

public abstract class JSCourseDisplayNode extends JSDisplayNode implements CourseDisplayNode {
  protected double ascent;
  protected double descent;
  protected double converse;
  protected double transverseBaseline;

  protected JSCourseDisplayNode(JSDisplay display, HTMLElement element) {
    super(display, element);
  }

  @Override
  public final double ascent() {
    return ascent;
  }

  @Override
  public final double descent() {
    return descent;
  }

  @Override
  public double baselineTransverse() {
    return transverseBaseline;
  }

  @Override
  public final void setBaselineTransverse(double baseline, boolean animate) {
    this.transverseBaseline = baseline;
    setJSPositionInternal(
        display.convert.unconvertTransverse(
            transverseCorner(),
            js().clientWidth,
            js().clientHeight),
        animate);
  }

  @Override
  public void setBaselinePosition(Vector vector, boolean animate) {
    this.converse = vector.converse;
    this.transverseBaseline = vector.transverse;
    fixPosition(animate);
  }

  @Override
  public double converse() {
    return converse;
  }

  @Override
  public double transverseSpan() {
    return ascent + descent;
  }

  @Override
  public final void setConverse(double converse, boolean animate) {
    this.converse = converse;
    setJSPositionInternal(
        display.convert.unconvertConverse(
            converseCorner(),
            js().clientWidth,
            js().clientHeight),
        animate);
  }
}
