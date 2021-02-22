package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.editor.display.DisplayNode;

public abstract class MockeryDisplayNode implements DisplayNode {

	private int converse;
	private int transverse;

	@Override
	public double converse() {
		return converse;
	}

	@Override
	public double baselineTransverse() {
		return transverse;
	}

	@Override
	public void setConverse(final double converse, final boolean animate) {
		this.converse = converse;
	}

	@Override
	public void setBaselineTransverse(final double transverse, final boolean animate) {
		this.transverse = transverse;
	}
}
