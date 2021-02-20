package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public class MockeryBlank extends MockeryCourseDisplayNode implements Blank {
	private int converseSpan;

	@Override
	public int converseSpan() {
		return converseSpan;
	}

	@Override
	public int ascent() {
		return 0;
	}

	@Override
	public int descent() {
		return 0;
	}

	@Override
	public void setConverseSpan(final Context context, final int span) {
		this.converseSpan = span;
	}
}
