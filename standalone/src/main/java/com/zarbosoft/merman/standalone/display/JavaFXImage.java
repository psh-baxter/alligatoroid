package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Image;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

public class JavaFXImage extends JavaFXNode implements Image {
	protected final ImageView view = new ImageView();

	protected JavaFXImage(JavaFXDisplay display) {
		super(display);
	}

	@Override
	public void setImage(final Context context, final String path) {
		view.setImage(new javafx.scene.image.Image(path.toString()));
		fixPosition();
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
