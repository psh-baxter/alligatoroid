package com.zarbosoft.merman.core.display;

import com.zarbosoft.rendaw.common.ROList;

/**
 * Combines transforms for multiple other visual nodes. Transforming a group with one node with 0
 * transverse/converse offset is the same as transforming the node directly with no group.
 *
 * <p>Converse span should be the span from 0 to the child with the maximum span, >= 0. Converse
 * edge is the greatest converse edge of the children.
 */
public interface Group extends FreeDisplayNode {
  void add(int index, DisplayNode node);

  default void add(final DisplayNode node) {
    add(size(), node);
  }

  void setTransverse(double transverse, boolean animate);

  @Override
  default double transverseSpan() {
    return 0;
  }

  @Override
  default double transverseEdge() {
    return 0;
  }

  void addAll(int index, ROList<? extends DisplayNode> nodes);

  default void remove(final int index) {
    remove(index, 1);
  }

  void remove(int index, int count);

  void remove(DisplayNode node);

  int size();

  void clear();
}
