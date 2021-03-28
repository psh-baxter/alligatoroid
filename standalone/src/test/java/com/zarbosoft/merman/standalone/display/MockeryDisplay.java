package com.zarbosoft.merman.standalone.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MockeryDisplay extends Display {
	int edge = 10000;
	int transverseEdge = 10000;
	List<DoubleListener> converseEdgeListeners = new ArrayList<>();
	List<DoubleListener> transverseEdgeListeners = new ArrayList<>();
	List<Consumer<HIDEvent>> hidEventListeners = new ArrayList<>();
	List<DisplayNode> nodes = new ArrayList<>();

	public MockeryDisplay() {
		super(converseDirection, transverseDirection);
	}

	@Override
	public Group group() {
		return new MockeryGroup();
	}

	@Override
	public Image image() {
		return new MockeryImage();
	}

	@Override
	public Text text() {
		return new MockeryText();
	}

	@Override
	public Font font(final String font, final double fontSize) {
		return new MockeryFont(fontSize);
	}

	@Override
	public Drawing drawing() {
		return new MockeryDrawing();
	}

	@Override
	public Blank blank() {
		return new MockeryBlank();
	}

	@Override
	public void addMouseExitListener(final Runnable listener) {

	}

	@Override
	public void addMouseMoveListener(final Consumer<Vector> listener) {

	}

	@Override
	public void addHIDEventListener(final Consumer<HIDEvent> listener) {
		hidEventListeners.add(listener);
	}

	@Override
	public void addTypingListener(final Consumer<String> listener) {

	}

    @Override
	public int edge(final Context context) {
		return edge;
	}

	@Override
	public void addConverseEdgeListener(final DoubleListener listener) {
		converseEdgeListeners.add(listener);
	}

	@Override
	public void removeConverseEdgeListener(final DoubleListener listener) {
		converseEdgeListeners.remove(listener);
	}

	@Override
	public int transverseEdge(final Context context) {
		return transverseEdge;
	}

	@Override
	public void addTransverseEdgeListener(final DoubleListener listener) {
		transverseEdgeListeners.add(listener);
	}

	@Override
	public void removeTransverseEdgeListener(final DoubleListener listener) {
		transverseEdgeListeners.remove(listener);
	}

	@Override
	public void add(final int index, final DisplayNode node) {
		nodes.add(index, node);
	}

	@Override
	public int childCount() {
		return nodes.size();
	}

	@Override
	public void remove(final DisplayNode node) {
		nodes.remove(node);
	}

	@Override
	public void setBackgroundColor(final ModelColor color) {

	}

	public void setConverseEdge(final Context context, final int converse) {
		final int oldEdge = edge;
		edge = converse;
		converseEdgeListeners.forEach(listener -> listener.changed(oldEdge, converse));
	}

	public void setTransverseEdge(final Context context, final int transverse) {
		final int oldEdge = transverseEdge;
		transverseEdge = transverse;
		transverseEdgeListeners.forEach(listener -> listener.changed(oldEdge, transverse));
	}

	public void sendHIDEvent(final HIDEvent event) {
		hidEventListeners.forEach(listener -> listener.accept(event));
	}
}
