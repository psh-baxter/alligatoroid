package com.zarbosoft.merman.editor.display;

public interface Font {
	int getAscent();

	int getDescent();

	int getWidth(String text);

	/**
	 * Index of first character that starts at or before converse (relative to start of text)
	 * @param text
	 * @param converse
	 * @return
	 */
	int getIndexAtConverse(String text, int converse);
}
