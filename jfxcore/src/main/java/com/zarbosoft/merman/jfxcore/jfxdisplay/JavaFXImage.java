package com.zarbosoft.merman.jfxcore.jfxdisplay;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Image;
import com.zarbosoft.merman.core.visual.Vector;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;

public class JavaFXImage extends JavaFXCommonBaseNode implements Image, CourseDisplayNode {
	protected double ascent;
	protected double descent;
	protected double converse;
	protected double transverseBaseline;

	protected JavaFXImage(JavaFXDisplay display) {
		super(display, new ImageView());
	}

	@Override
	public void setImage(final Context context, final String path) {
		ImageView image = (ImageView) this.node;
		image.setImage(new javafx.scene.image.Image(path.toString()));
		Bounds bounds = image.getLayoutBounds();
		ascent = bounds.getHeight();
		fixPosition();
	}

	@Override
	public void rotate(final Context context, final double rotate) {
		ImageView image = (ImageView) this.node;
		image.setRotate(rotate);
		Bounds bounds = image.getLayoutBounds();
		ascent = bounds.getHeight();
		fixPosition();
	}

	@Override
	protected double transverseCorner() {
		return transverseBaseline - ascent;
	}

	@Override
	protected double converseCorner() {
		return converse;
	}

	@Override
	public final double ascent() {
	  return ascent;
	}

	@Override
	public final double descent() {
	  return descent;
	}

	@Override
	public double baselineTransverse() {
	  return transverseBaseline;
	}

	@Override
	public final void setBaselineTransverse(double baseline, boolean animate) {
	  this.transverseBaseline = baseline;
	  Bounds bounds = node.getLayoutBounds();
	  setJFXPositionInternal(
		  display.convert.unconvertTransverse(
			  transverseCorner(), bounds.getWidth(), bounds.getHeight()),
		  animate);
	}

	@Override
	public void setBaselinePosition(Vector vector, boolean animate) {
	  this.converse = vector.converse;
	  this.transverseBaseline = vector.transverse;
	  fixPosition(animate);
	}

	@Override
	public double converse() {
	  return converse;
	}

	@Override
	public double transverseSpan() {
	  return ascent + descent;
	}

	@Override
	public final void setConverse(double converse, boolean animate) {
	  this.converse = converse;
	  Bounds bounds = node.getLayoutBounds();
	  setJFXPositionInternal(
		  display.convert.unconvertConverse(converseCorner(), bounds.getWidth(), bounds.getHeight()),
		  animate);
	}
}
