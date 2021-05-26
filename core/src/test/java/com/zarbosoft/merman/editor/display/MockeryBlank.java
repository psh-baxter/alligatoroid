package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.Context;

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

	public void setConverseSpan(final Context context, final double span) {
		this.converseSpan = span;
	}
}
