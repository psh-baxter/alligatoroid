package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.visual.Vector;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

public class JavaFXImage extends JavaFXNode implements Image {
	protected final ImageView view = new ImageView();

	@Override
	public void setImage(final Context context, final String path) {
		final Vector at = position(context);
		view.setImage(new javafx.scene.image.Image(path.toString()));
		setPosition(context, at, false);
	}

	@Override
	public void rotate(final Context context, final double rotate) {
		view.setRotate(rotate);
	}

	@Override
	protected Node node() {
		return view;
	}
}
