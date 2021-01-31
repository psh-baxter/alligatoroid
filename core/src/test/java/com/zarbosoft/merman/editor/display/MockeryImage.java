package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public class MockeryImage extends MockeryDisplayNode implements Image {
	@Override
	public void setImage(final Context context, final String path) {

	}

	@Override
	public void rotate(final Context context, final double rotate) {

	}

	@Override
	public int converseSpan(final Context context) {
		return 25;
	}

	@Override
	public int transverseSpan(final Context context) {
		return 25;
	}
}
