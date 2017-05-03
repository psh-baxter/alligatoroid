package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;
import com.zarbosoft.bonestruct.syntax.NodeType;

import java.util.Map;

public class Node {
	public Value.Parent parent;
	public NodeType type;
	public final Map<String, Value> data;
	public VisualNodeType visual;

	public Node(final NodeType type, final Map<String, Value> data) {
		this.type = type;
		this.data = data;
		data.forEach((k, v) -> {
			v.setParent(new Parent() {
				@Override
				public Node node() {
					return Node.this;
				}
			});
		});
	}

	/**
	 * Only for document root.
	 *
	 * @param data
	 */
	public Node(final Map<String, Value> data) {
		this.type = null;
		this.data = data;
	}

	public Path getPath() {
		if (parent == null)
			return new Path();
		else
			return parent.getPath();
	}

	public Visual createVisual(final Context context) {
		this.visual = type.createVisual(context, this);
		return visual;
	}

	public void setParent(final Value.Parent parent) {
		this.parent = parent;
	}

	public VisualNodeType getVisual() {
		return visual;
	}

	public static abstract class Parent {
		public abstract Node node();
	}
}
