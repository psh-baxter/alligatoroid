package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.syntax.Direction;
import com.zarbosoft.merman.syntax.style.ModelColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MockeryDisplay extends Display {
  int width = 10000;
  int height = 10000;
  List<Consumer<HIDEvent>> hidEventListeners = new ArrayList<>();
  List<DisplayNode> nodes = new ArrayList<>();

  public MockeryDisplay(Direction converseDirection, Direction transverseDirection) {
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
  public Font font(final String font, final int fontSize) {
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
  public void addHIDEventListener(final Consumer<HIDEvent> listener) {
    hidEventListeners.add(listener);
  }

  @Override
  public void addTypingListener(final Consumer<String> listener) {}

  @Override
  public double width() {
    return width;
  }

  @Override
  public double height() {
    return height;
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
  public void setBackgroundColor(final ModelColor color) {}

  public void setWidth(final int width) {
    widthChanged(width);
  }

  public void setHeight(final int height) {
    heightChanged(height);
  }

  public void sendHIDEvent(final HIDEvent event) {
    for (Consumer<HIDEvent> listener : hidEventListeners) {
      listener.accept(event);
    }
  }
}
