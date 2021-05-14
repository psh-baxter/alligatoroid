package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;

public class MockeryBlank extends MockeryCourseDisplayNode implements Blank {
	private double converseSpan;

	@Override
	public double converseSpan() {
		return converseSpan;
	}

	@Override
	public Object inner_() {
		return null;
	}

	@Override
	public double ascent() {
		return 0;
	}

	@Override
	public double descent() {
		return 0;
	}

	@Override
	public void setConverseSpan(final Context context, final double span) {
		this.converseSpan = span;
	}
}
