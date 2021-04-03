package com.zarbosoft.merman.core.display;

import com.zarbosoft.rendaw.common.ROList;

/**
 * doubles as loose collection and box
 * span, edge based on max of all children
 * negative positions ignored
 */
public interface Group extends FreeDisplayNode {
	void add(int index, DisplayNode node);

	default void add(final DisplayNode node) {
		add(size(), node);
	}

	void setTransverse(double transverse, boolean animate);

	void addAll(int index, ROList<? extends DisplayNode> nodes);

	default void remove(final int index) {
		remove(index, 1);
	}

	void remove(int index, int count);

	void remove(DisplayNode node);

	int size();

	void clear();

}
