package com.zarbosoft.merman.editor.display;

public interface Font {
	double getAscent();

	double getDescent();

	double getWidth(String text);

	/**
	 * Index of first character that starts at or before converse (relative to start of text)
	 * @param text
	 * @param converse
	 * @return
	 */
	int getIndexAtConverse(String text, double converse);
}
