package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.syntax.style.ModelColor;

public interface Text extends CourseDisplayNode {
	String text();

	void setText(Context context, String text);

	void setColor(Context context, ModelColor color);

	Font font();

	void setFont(Context context, Font font);

	/**
	 * Get the nearest index to the converse - so halfway through 1 = 0, halfway through last = last
	 * @param context
	 * @param converse
	 * @return
	 */
	default int getIndexAtConverse(final Context context, final double converse) {
		return font().measurer().getIndexAtConverse(context, text(), converse);
	}

	double getConverseAtIndex(final int index);
}
