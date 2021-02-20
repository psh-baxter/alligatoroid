package com.zarbosoft.merman.editor.details;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.FreeDisplayNode;

public abstract class DetailsPage {
	public int priority = 0;
	public FreeDisplayNode node;

	public abstract void tagsChanged(Context context);
}
