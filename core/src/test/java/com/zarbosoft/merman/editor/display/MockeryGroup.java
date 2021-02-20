package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class MockeryGroup extends MockFreeDisplayNode implements Group {
  TSList<MockeryDisplayNode> nodes = new TSList<>();

  @Override
  public void add(final int index, final DisplayNode node) {
    nodes.insert(index, (MockeryDisplayNode) node);
  }

  @Override
  public void setTransverse(int transverse, boolean animate) {
    setPosition(new Vector(converse(), transverse), animate);
  }

  @Override
  public void addAll(final int index, final ROList<? extends DisplayNode> nodes) {
    this.nodes.insertAll(index, (ROList) nodes);
  }

  @Override
  public void remove(final int index, final int count) {
    nodes.sublist(index, index + count - 1).clear();
  }

  @Override
  public void remove(final DisplayNode node) {
    nodes.removeVal((MockeryDisplayNode) node);
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
  public int converseSpan() {
    int max = 0;
    for (MockeryDisplayNode node : nodes) {
      int got = node.converseEdge();
      if (got > max) max = got;
    }
    return max;
  }

  @Override
  public int transverseSpan() {
    int max = 0;
    for (MockeryDisplayNode node : nodes) {
      int got = node.transverseEdge();
      if (got > max) max = got;
    }
    return max;
  }

  public int count() {
    return nodes.size();
  }

  public MockeryDisplayNode get(final int index) {
    return nodes.get(index);
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    transverse = vector.transverse;
    converse = vector.converse;
  }
}
