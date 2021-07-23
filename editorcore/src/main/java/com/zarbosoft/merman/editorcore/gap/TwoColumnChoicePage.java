package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.displayderived.Box;
import com.zarbosoft.merman.editorcore.displayderived.BoxContainer;
import com.zarbosoft.merman.editorcore.displayderived.ColumnarTableLayout;
import com.zarbosoft.merman.editorcore.displayderived.Container;
import com.zarbosoft.merman.editorcore.displayderived.ConvScrollContainer;
import com.zarbosoft.merman.editorcore.displayderived.PadContainer;
import com.zarbosoft.merman.editorcore.displayderived.StackGroup;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class TwoColumnChoicePage {
  public final TSList<TwoColumnChoice> choices;
  private final Box highlight;
  private final Context.ContextDoubleListener edgeListener;
  private final ConvScrollContainer scroller = new ConvScrollContainer();
  public Container displayRoot;
  TSList<ROPair<CourseDisplayNode, CourseDisplayNode>> rows = TSList.of();
  private int index = 0;

  public TwoColumnChoicePage(Editor editor, TSList<TwoColumnChoice> choices) {
    this.choices = choices;
    this.edgeListener =
        new Context.ContextDoubleListener() {
          @Override
          public void changed(Context context, double oldValue, double newValue) {
            updateScroll(context);
          }
        };
    editor.context.addConverseEdgeListener(edgeListener);
    final StackGroup group = new StackGroup(editor.context);

    highlight = new Box(editor.context);
    highlight.setStyle(editor.choiceCursorStyle);
    group.add(highlight.drawing);

    ColumnarTableLayout table =
        new ColumnarTableLayout(editor.context, editor.detailSpan * editor.context.toPixels);
    table.setRowStride(editor.context, editor.choiceRowStride);
    table.setOuterColumnGap(editor.context, editor.choiceColumnSpace);
    table.setRowPadding(editor.context, editor.choiceRowPadding);
    group.addRoot(table);

    for (final TwoColumnChoice choice : choices) {
      ROPair<CourseDisplayNode, CourseDisplayNode> display = choice.display(editor);
      rows.add(display);
      table.add(TSList.of(display.first, display.second));
    }
    table.layout();
    scroller.inner = group;
    changeChoice(editor.context, 0);
    displayRoot =
        new BoxContainer(
            editor.context,
            editor.detailStyle,
            new PadContainer(editor.context, editor.detailPad).addRoot(scroller));
  }

  public void updateScroll(final Context context) {
    final ROPair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final DisplayNode preview = row.first;
    final DisplayNode text = row.second;
    final double converse = preview.converse();
    final double converseEdge = text.converseEdge();
    scroller.scrollVisible(converse, converseEdge, context.animateDetails);
  }

  private void changeChoice(final Context context, final int index) {
    this.index = index;
    final ROPair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final CourseDisplayNode preview = row.first;
    final CourseDisplayNode text = row.second;
    final double converse = preview.converse();
    final double transverse = Math.min(preview.transverse(), text.transverse());
    final double converseEdge = text.converseEdge();
    final double transverseEdge = Math.max(preview.transverseEdge(), text.transverseEdge());
    highlight.setSize(context, converseEdge - converse, transverseEdge - transverse);
    highlight.setPosition(new Vector(converse, transverse), false);
    updateScroll(context);
  }

  public void destroy(final Context context) {
    context.removeConverseEdgeListener(edgeListener);
  }

  public void choose(Editor editor) {
    choices.get(index).choose(editor, null);
  }

  public void nextChoice(Context context) {
    changeChoice(context, (index + 1) % choices.size());
  }

  public void previousChoice(Context context) {
    changeChoice(context, (index + choices.size() - 1) % choices.size());
  }
}
