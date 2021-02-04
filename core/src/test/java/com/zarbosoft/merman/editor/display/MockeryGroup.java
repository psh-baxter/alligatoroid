package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockeryGroup extends MockeryDisplayNode implements Group {
	TSList<MockeryDisplayNode> nodes = new TSList<>();

	@Override
	public void add(final int index, final DisplayNode node) {
		nodes.insert(index, (MockeryDisplayNode) node);
	}

	@Override
	public void addAll(final int index, final ROList<? extends DisplayNode> nodes) {
		this.nodes.insertAll(index, (ROList)nodes);
	}

	@Override
	public void remove(final int index, final int count) {
		nodes.sublist(index, index + count - 1).clear();
	}

	@Override
	public void remove(final DisplayNode node) {
		nodes.removeVal((MockeryDisplayNode)node);
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
}
