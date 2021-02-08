package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Vector;

public interface Drawing extends DisplayNode {

	void clear();

	void resize(Context context, Vector vector);

	DrawingContext begin(Context context);

}
