package com.zarbosoft.merman.editorcore.details;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.FreeDisplayNode;

public abstract class DetailsPage {
	public int priority = 0;
	public FreeDisplayNode node;

	public abstract void tagsChanged(Context context);
}
