package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.editor.display.Group;
import com.zarbosoft.merman.core.editor.display.Text;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.editor.display.MockeryGroup;
import com.zarbosoft.merman.editorcore.displayderived.CLayout;
import com.zarbosoft.merman.editorcore.displayderived.ColumnarTableLayout;
import com.zarbosoft.merman.editorcore.displayderived.RowLayout;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDisplayDerived {

  @Test
  public void testColumnarTableLayout() {
    final MockeryDisplay display = new MockeryDisplay(Direction.RIGHT, Direction.DOWN);
    final ColumnarTableLayout layout = new ColumnarTableLayout(display, 25);
    /*
    // TODO implement course display node wrapper around group
    {
    	final Group leftGroup = display.group();
    	final Text left = display.text();
    	left.setText(null, "1");
    	left.setBaselineTransverse(8);
    	leftGroup.add(left);
    	final Text right = display.text();
    	right.setText(null, "aaa");
    	layout.add(TSList.of(leftGroup, right));
    }
     */
    {
      final Text left = display.text();
      left.setText(null, "333");
      final Text right = display.text();
      right.setText(null, "bb");
      layout.add(TSList.of(left, right));
    }
    {
      final Text left = display.text();
      left.setText(null, "22");
      final Text right = display.text();
      right.setText(null, "c");
      layout.add(TSList.of(left, right));
    }
    {
      final Text left = display.text();
      left.setText(null, "4444");
      final Text right = display.text();
      right.setText(null, "dddd");
      layout.add(TSList.of(left, right));
    }
    layout.layout();
    int index = 0;
    for (final Pair<Integer, Integer> pair :
        TSList.of(
            /*
            new Pair<>(0, 0),
            new Pair<>(30, 10),
                 */
            new Pair<>(0, 00),
            new Pair<>(30, 00),
            new Pair<>(0, 10),
            new Pair<>(30, 10),
            new Pair<>(50, 0),
            new Pair<>(90, 0))) {
      final int index2 = index++;
      assertThat(
          Format.format("for index %s, converse", index2),
              (int)((MockeryGroup) layout.group).get(index2).converse(),
          equalTo(pair.first));
      assertThat(
          Format.format("for index %s, transverse", index2),
              (int)((MockeryGroup) layout.group).get(index2).transverse(),
          equalTo(pair.second));
    }
  }

  @Test
  public void testCLayout() {
    final MockeryDisplay display = new MockeryDisplay(Direction.RIGHT, Direction.DOWN);
    final CLayout layout = new CLayout(display);
    {
      final Group itemGroup = display.group();
      final Text item = display.text();
      item.setText(null, "dog");
      item.setBaselineTransverse(8);
      itemGroup.add(item);
      layout.add(itemGroup);
    }
    {
      final Text item = display.text();
      item.setText(null, "donut");
      layout.add(item);
    }
    {
      final Group itemGroup = display.group();
      final Text item = display.text();
      item.setText(null, "9");
      item.setBaselineTransverse(8);
      itemGroup.add(item);
      layout.add(itemGroup);
    }
    {
      final Text item = display.text();
      item.setText(null, "apple");
      layout.add(item);
    }
    layout.layout();
    int index = 0;
    for (final int converse : TSList.of(0, 30, 80, 90)) {
      final int index2 = index++;
      assertThat(
          Format.format("for index %s, converse", index2),
              (int)((MockeryGroup) layout.group).get(index2).converse(),
          equalTo(converse));
    }
  }

  @Test
  public void testRowLayout() {
    final MockeryDisplay display = new MockeryDisplay(Direction.RIGHT, Direction.DOWN);
    final RowLayout layout = new RowLayout(display);
    {
      final Group itemGroup = display.group();
      final Text item = display.text();
      item.setText(null, "dog");
      item.setBaselineTransverse(8);
      itemGroup.add(item);
      layout.add(itemGroup);
    }
    {
      final Text item = display.text();
      item.setText(null, "donut");
      layout.add(item);
    }
    {
      final Group itemGroup = display.group();
      final Text item = display.text();
      item.setText(null, "9");
      item.setBaselineTransverse(8);
      itemGroup.add(item);
      layout.add(itemGroup);
    }
    {
      final Text item = display.text();
      item.setText(null, "apple");
      layout.add(item);
    }
    layout.layout();
    int index = 0;
    for (final Pair<Integer, Integer> pair :
        TSList.of(new Pair<>(0, 0), new Pair<>(30, 2), new Pair<>(80, 0), new Pair<>(90, 2))) {
      final int index2 = index++;
      assertThat(
          Format.format("for index %s, converse", index2),
              (int) ((MockeryGroup) layout.group).get(index2).converse(),
          equalTo(pair.first));
      assertThat(
          Format.format("for index %s, transverse", index2),
              (int)((MockeryGroup) layout.group).get(index2).transverse(),
          equalTo(pair.second));
    }
  }
}
