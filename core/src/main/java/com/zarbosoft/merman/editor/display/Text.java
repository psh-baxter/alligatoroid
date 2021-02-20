package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.syntax.style.ModelColor;

public interface Text extends CourseDisplayNode {
	String text();

	void setText(Context context, String text);

	void setColor(Context context, ModelColor color);

	Font font();

	void setFont(Context context, Font font);

	default int getIndexAtConverse(final Context context, final int converse) {
		return font().getIndexAtConverse(text(), converse);
	}

	int getConverseAtIndex(final int index);
}
