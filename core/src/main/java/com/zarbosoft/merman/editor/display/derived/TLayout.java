package com.zarbosoft.merman.editor.display.derived;

import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;

import java.util.ArrayList;
import java.util.List;

public class TLayout {
	private final Group group;
	List<DisplayNode> nodes = new ArrayList<>();

	public TLayout(final Group group) {
		this.group = group;
	}

	public void add(final DisplayNode node) {
		group.add(node);
	}

	public void layout() {
		int transverse = 0;
		for (final DisplayNode node : nodes) {
			node.setTransverse(transverse, false);
			transverse += node.transverseSpan();
		}
	}
}
