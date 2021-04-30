package com.zarbosoft.merman.editorcore.display;

import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Drawing;
import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.display.Image;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.ModelColor;

import java.util.ArrayList;
import java.util.List;

public class MockeryDisplay extends Display {
  List<DisplayNode> nodes = new ArrayList<>();

  public MockeryDisplay(Direction converseDirection, Direction transverseDirection) {
    super(converseDirection, transverseDirection);
    setWidth(10000);
    setHeight(10000);
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
    return new MockeryFont((int) fontSize);
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

  @Override
  public double toPixels(Syntax.DisplayUnit displayUnit) {
    return 1;
  }

  public void setWidth(final int width) {
    widthChanged(width);
  }

  public void setHeight(final int height) {
    heightChanged(height);
  }

  public void sendHIDEvent(final ButtonEvent event) {
    keyEventListener.apply(event);
  }
}
