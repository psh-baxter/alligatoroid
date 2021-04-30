package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.details.DetailsPage;
import com.zarbosoft.merman.editorcore.displayderived.Box;
import com.zarbosoft.merman.editorcore.displayderived.ColumnarTableLayout;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class TwoColumnChoicePage extends DetailsPage {
  private final Box highlight;
  private final Group tableGroup;
  private final Context.ContextDoubleListener edgeListener;
  public final TSList<TwoColumnChoice> choices;
  TSList<ROPair<CourseDisplayNode, CourseDisplayNode>> rows = TSList.of();
  private int index = 0;
  private double scroll = 0;

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
    final Group group = editor.context.display.group();
    this.node = group;

    highlight = new Box(editor.context);
    highlight.setStyle(editor.choiceCursorStyle);
    group.add(highlight.drawing);

    final ColumnarTableLayout table = new ColumnarTableLayout(editor.context, editor.context.syntax.detailSpan);
    tableGroup = table.group;
    group.add(table.group);

    for (final TwoColumnChoice choice : choices) {
      ROPair<CourseDisplayNode, CourseDisplayNode> display = choice.display(editor);
      rows.add(display);
      table.add(TSList.of(display.first, display.second));
    }
    table.layout();
    changeChoice(editor.context, 0);
  }

  public void updateScroll(final Context context) {
    final ROPair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final DisplayNode preview = row.first;
    final DisplayNode text = row.second;
    final double converse = preview.converse();
    final double converseEdge = text.converseEdge();
    scroll = Math.min(converse, Math.max(converseEdge - context.edge, scroll));
    tableGroup.setConverse(scroll, context.animateDetails);
  }

  private void changeChoice(final Context context, final int index) {
    this.index = index;
    final ROPair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final CourseDisplayNode preview = row.first;
    final CourseDisplayNode text = row.second;
    final double converse = preview.converse();
    final double transverse = Math.min(preview.baselineTransverse(), text.baselineTransverse());
    final double converseEdge = text.converseEdge();
    final double transverseEdge =
        Math.max(preview.transverseEdge(), text.transverseEdge());
    highlight.setSize(context, converseEdge - converse, transverseEdge - transverse);
    highlight.setPosition(new Vector(converse, transverse), false);
    updateScroll(context);
  }

  public void destroy(final Context context) {
    context.removeConverseEdgeListener(edgeListener);
  }

  @Override
  public void tagsChanged(final Context context) {}

  public void choose(Editor editor) {
    choices.get(index).choose(editor);
  }

  public void nextChoice(Context context) {
    changeChoice(context, (index + 1) % choices.size());
  }

  public void previousChoice(Context context) {
    changeChoice(context, (index + choices.size() - 1) % choices.size());
  }
}
