package com.zarbosoft.bonestruct.display;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;

import java.util.function.Consumer;

public interface Display {
	Group group();

	Image image();

	Text text();

	Font font(String font, int fontSize);

	Drawing drawing();

	Blank blank();

	void addMouseExitListener(Runnable listener);

	void addMouseMoveListener(Consumer<Vector> listener);

	void addHIDEventListener(Consumer<HIDEvent> listener);

	void addTypingListener(Consumer<String> listener);

	void focus();

	@FunctionalInterface
	interface IntListener {
		void changed(int oldValue, int newValue);
	}

	int edge(Context context);

	void addConverseEdgeListener(IntListener listener);

	int transverseEdge(Context context);

	void addTransverseEdgeListener(IntListener listener);

	void add(int index, DisplayNode node);

	default void add(final DisplayNode node) {
		add(size(), node);
	}

	int size();

	void remove(DisplayNode node);

	void setBackgroundColor(ModelColor color);
}
