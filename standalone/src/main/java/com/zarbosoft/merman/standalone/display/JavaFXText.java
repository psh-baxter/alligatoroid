package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;

public class JavaFXText extends JavaFXNode implements Text {
	protected final javafx.scene.text.Text text = new javafx.scene.text.Text();

	protected JavaFXText(JavaFXDisplay display) {
		super(display);
		text.setTextOrigin(VPos.BASELINE);
	}

	@Override
	public String text() {
		return text.getText();
	}

	public void setText(final Context context, final String newText) {
		text.setText(newText);
		fixPosition();
	}

	@Override
	public void setColor(final Context context, final ModelColor color) {
		text.setFill(Helper.convert(color));
	}

	@Override
	public Font font() {
		return new JavaFXFont(text.getFont());
	}

	@Override
	public void setFont(final Context context, final Font font) {
		text.setFont(((JavaFXFont) font).font);
	}

	public String getText() {
		return text.getText();
	}

	@Override
	protected Node node() {
		return text;
	}

	public int getIndexAtConverse(final Context context, final double converse) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return text.hitTest(new Point2D(text.getX(), converse - converse())).getInsertionIndex();
			case LEFT:
			case RIGHT:
				return text.hitTest(new Point2D(converse - converse(), text.getY())).getInsertionIndex();
		}
		throw new DeadCode();
	}

	public double getConverseAtIndex(final int index) {
		if (index == 0)
			return 0;
		text.getText().substring(0, 0);
		final Font font = font();
		final double precedingLength = font.getWidth(text.getText().substring(0, index));
		final double charLength = font.getWidth(text
				.getText()
				.substring(Math.max(0, index - 1), Math.min(text.getText().length(), Math.max(1, index))));
		return (int) (precedingLength - charLength * 0.2);
	}

	@Override
	public void setBaselinePosition(final Vector vector, final boolean animate) {
		int x = 0;
		int y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = -vector.converse - (int) node().getLayoutBounds().getHeight();
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = -vector.converse - (int) node().getLayoutBounds().getWidth();
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = -vector.transverse - (int) node().getLayoutBounds().getHeight();
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = -vector.transverse - (int) node().getLayoutBounds().getWidth();
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		if (animate)
			new TransitionSmoothOut(text, x - node().getLayoutX(), y - node().getLayoutY()).play();
		node().setLayoutX(x);
		node().setLayoutY(y);
	}
}
