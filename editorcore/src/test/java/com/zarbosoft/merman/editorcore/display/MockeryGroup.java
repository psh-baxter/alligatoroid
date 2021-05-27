package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.rendaw.common.TSList;

public class MockeryGroup extends MockFreeDisplayNode implements Group {
  TSList<DisplayNode> nodes = new TSList<>();

  @Override
  public void add(final int index, final DisplayNode node) {
    nodes.insert(index, node);
  }

  @Override
  public void setTransverse(double transverse, boolean animate) {
    setPosition(new Vector(converse(), transverse), animate);
  }

  @Override
  public void remove(final int index, final int count) {
    nodes.sublist(index, index + count - 1).clear();
  }

  @Override
  public void remove(final DisplayNode node) {
    nodes.removeVal(node);
  }

  @Override
  public int size() {
    return nodes.size();
  }

  @Override
  public void clear() {
    nodes.clear();
  }

  @Override
  public double converseSpan() {
    double max = 0;
    for (DisplayNode node : nodes) {
      double got = node.converseEdge();
      if (got > max) max = got;
    }
    return max;
  }

  @Override
  public Object inner_() {
    return null;
  }

  @Override
  public double transverseSpan() {
    double max = 0;
    for (DisplayNode node : nodes) {
      double got = node.transverseEdge();
      if (got > max) max = got;
    }
    return max;
  }

  public int count() {
    return nodes.size();
  }

  public DisplayNode get(final int index) {
    return (DisplayNode) nodes.get(index);
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    transverse = vector.transverse;
    converse = vector.converse;
  }
}
