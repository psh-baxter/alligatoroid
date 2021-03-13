package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public class MockeryFont implements Font {
	int size = 10;

	public MockeryFont(final int fontSize) {
	}

	@Override
	public double getAscent() {
		return (size * 8) / 10;
	}

	@Override
	public double getDescent() {
		return (size * 2) / 10;
	}

	@Override
	public Measurer measurer() {
		return new Measurer() {
			@Override
			public double getWidth(String text) {
				return size * text.length();
			}

			@Override
			public int getIndexAtConverse(Context context, String text, double converse) {
				return (int) Math.min(text.length(), Math.round((double)converse / size));
			}
		};
	}
}
