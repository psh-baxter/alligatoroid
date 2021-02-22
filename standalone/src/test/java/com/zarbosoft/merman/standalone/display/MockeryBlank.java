package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;

public class MockeryBlank extends MockeryDisplayNode implements Blank {
	private int converse;
	private int transverse;

	@Override
	public double converseSpan() {
		return converse;
	}

	@Override
	public void setConverseSpan(final Context context, final double converse) {
		this.converse = converse;
	}
}
