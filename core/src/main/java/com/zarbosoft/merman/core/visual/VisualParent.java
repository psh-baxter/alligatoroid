package com.zarbosoft.merman.core.visual;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.rendaw.common.ROPair;

public abstract class VisualParent {
	public abstract Visual visual();

	public abstract VisualAtom atomVisual();

	public abstract Visual.ExtendBrickResult createPreviousBrick(Context context);

	public abstract Visual.ExtendBrickResult createNextBrick(Context context);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public abstract Brick findPreviousBrick(final Context context);

	public abstract Brick findNextBrick(final Context context);

	public abstract ROPair<Hoverable, Boolean> hover(Context context, Vector point);

	public abstract boolean selectPrevious(Context context);

	public abstract boolean selectNext(Context context);

	public abstract void notifyLastBrickCreated(Context context, Brick brick);

	public abstract void notifyFirstBrickCreated(Context context, Brick brick);
}
