package com.zarbosoft.merman.editor.display;

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
	public double getWidth(final String text) {
		return size * text.length();
	}

	@Override
	public int getIndexAtConverse(final String text, final double converse) {
		return (int) Math.min(text.length(), converse / size);
	}
}
