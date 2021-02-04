package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickImage extends Brick {
	private final Image image;
	private Style style;

	public BrickImage(final Context context, final BrickInterface inter) {
		super(inter);
		image = context.display.image();
		tagsChanged(context);
	}

	@Override
	public void tagsChanged(final Context context) {
		this.style = context.getStyle(inter.getTags(context).ro());
		alignment = inter.findAlignment(style);
		image.setImage(context, style.image);
		image.rotate(context, style.rotate);
		changed(context);
	}

	public Properties properties(final Context context, final Style style) {
		return new Properties(
				style.split,
				(int) image.transverseSpan(),
				(int) 0,
				inter.findAlignment(style),
				(int) image.converseSpan()
		);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {
		image.setTransverse(ascent, false);
	}

	@Override
	public int converseEdge() {
		return image.converseEdge();
	}

	@Override
	public int converseSpan() {
		return image.converseSpan();
	}

	@Override
	public int getConverse(final Context context) {
		return image.converse();
	}

	@Override
	public DisplayNode getDisplayNode() {
		return image;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.preAlignConverse = minConverse;
		image.setConverse(converse, false);
	}
}
