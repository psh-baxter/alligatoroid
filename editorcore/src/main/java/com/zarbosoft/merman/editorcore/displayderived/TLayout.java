package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.editor.display.CourseDisplayNode;
import com.zarbosoft.merman.core.editor.display.Group;

import java.util.ArrayList;
import java.util.List;

public class TLayout {
	private final Group group;
	List<CourseDisplayNode> nodes = new ArrayList<>();

	public TLayout(final Group group) {
		this.group = group;
	}

	public void add(final CourseDisplayNode node) {
		group.add(node);
	}

	public void layout() {
		double transverse = 0;
		for (final CourseDisplayNode node : nodes) {
			node.setBaselineTransverse(transverse, false);
			transverse += node.transverseSpan();
		}
	}
}
