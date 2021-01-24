package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;

public abstract class MockeryDisplayNode implements DisplayNode {

	private int converse;
	private int transverse;

	@Override
	public int converse(final Context context) {
		return converse;
	}

	@Override
	public int transverse(final Context context) {
		return transverse;
	}

	@Override
	public void setConverse(final Context context, final int converse, final boolean animate) {
		this.converse = converse;
	}

	@Override
	public void setTransverse(final Context context, final int transverse, final boolean animate) {
		this.transverse = transverse;
	}
}
