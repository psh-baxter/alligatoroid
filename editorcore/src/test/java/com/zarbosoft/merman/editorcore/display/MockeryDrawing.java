package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.DrawingContext;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;

public class MockeryDrawing extends MockeryDisplayNode implements Drawing {
	private Vector size = new Vector(0, 0);

	@Override
	public double converseSpan() {
		return size.converse;
	}


	@Override
	public void clear() {

	}

	@Override
	public void resize(final Context context, final Vector vector) {
		MockeryDrawing.this.size = vector;
	}

	@Override
	public DrawingContext begin(final Context context) {
		return new DrawingContext() {
			@Override
			public void setLineColor(final ModelColor color) {

			}

			@Override
			public void setLineCapRound() {

			}

			@Override
			public void setLineThickness(final double lineThickness) {

			}

			@Override
			public void setLineCapFlat() {

			}

			@Override
			public void setFillColor(final ModelColor color) {

			}

			@Override
			public void beginStrokePath() {

			}

			@Override
			public void beginFillPath() {

			}

			@Override
			public void moveTo(final double halfBuffer, final double halfBuffer1) {

			}

			@Override
			public void lineTo(final double i, final double i1) {

			}

			@Override
			public void closePath() {

			}

			@Override
			public void arcTo(final double c, final double t, final double c2, final double t2, final double radius) {

			}

			@Override
			public void translate(final double c, final double t) {

			}
		};
	}
}
