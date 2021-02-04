package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;

public abstract class MockeryDisplayNode implements DisplayNode {

	private int converse;
	private int transverse;

	@Override
	public int converse() {
		return converse;
	}

	@Override
	public int transverse() {
		return transverse;
	}

	@Override
	public void setConverse(final int converse, final boolean animate) {
		this.converse = converse;
	}

	@Override
	public void setTransverse(final int transverse, final boolean animate) {
		this.transverse = transverse;
	}
}
