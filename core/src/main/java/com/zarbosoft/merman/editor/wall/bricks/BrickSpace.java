package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickSpace extends Brick {
	private int converse = 0;
	private final Blank visual;

	public BrickSpace(final Context context, final BrickInterface inter) {
		super(inter);
		visual = context.display.blank();
		tagsChanged(context);
	}

	@Override
	public void tagsChanged(final Context context) {
		this.style = context.getStyle(inter.getTags(context).ro());
		alignment = inter.findAlignment(style);
		changed(context);
	}

	@Override
	public int converseEdge() {
		return converse + style.space;
	}

	@Override
	public int converseSpan() {
		return style.space;
	}

	@Override
	public Properties properties(final Context context, final Style style) {
		return new Properties(
				style.split,
				style.spaceTransverseBefore,
				style.spaceTransverseAfter,
				alignment,
				style.space + style.spaceBefore + style.spaceAfter
		);
	}

	@Override
	public DisplayNode getDisplayNode() {
		return visual;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.preAlignConverse = minConverse;
		this.converse = converse;
		visual.setPosition(new Vector(converse, 0), false);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {

	}

	@Override
	public int getConverse(final Context context) {
		return converse;
	}
}
