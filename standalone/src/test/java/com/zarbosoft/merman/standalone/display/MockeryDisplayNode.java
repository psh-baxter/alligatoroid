package com.zarbosoft.merman.standalone.display;

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
	public int baselineTransverse() {
		return transverse;
	}

	@Override
	public void setConverse(final int converse, final boolean animate) {
		this.converse = converse;
	}

	@Override
	public void setBaselineTransverse(final int transverse, final boolean animate) {
		this.transverse = transverse;
	}
}
