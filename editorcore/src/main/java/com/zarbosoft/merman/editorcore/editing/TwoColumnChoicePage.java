package com.zarbosoft.merman.editorcore.editing;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.CourseDisplayNode;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.derived.Box;
import com.zarbosoft.merman.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.List;

public class TwoColumnChoicePage extends DetailsPage {
  private final Box highlight;
  private final Group tableGroup;
  private final Context.ContextIntListener edgeListener;
  List<Pair<CourseDisplayNode, CourseDisplayNode>> rows = new ArrayList<>();
  private int index = 0;
  private int scroll = 0;

  public TwoColumnChoicePage(final Context context, List<TwoColumnChoice> choices) {
    this.edgeListener =
        new Context.ContextIntListener() {
          @Override
          public void changed(final Context context, final int oldValue, final int newValue) {
            updateScroll(context);
          }
        };
    context.addConverseEdgeListener(edgeListener);
    final Group group = context.display.group();
    this.node = group;

    final PSet<Tag> tags = context.getGlobalTags();

    BoxStyle.Baked highlightStyle =
        context.getStyle(tags.plus(new PartTag("details_selection")).plus(new PartTag("details")))
            .box;
    if (highlightStyle == null) highlightStyle = new BoxStyle.Baked();
    highlightStyle.merge(context.syntax.gapChoiceStyle);
    highlight = new Box(context);
    highlight.setStyle(highlightStyle);
    group.add(highlight.drawing);

    final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
    tableGroup = table.group;
    group.add(table.group);

    for (final TwoColumnChoice choice : choices) {
      Pair<DisplayNode, DisplayNode> display = choice.display(context);
      rows.add(display);
      table.add(ImmutableList.of(display.first, display.second));
    }
    table.layout();
    changeChoice(context, 0);
    final List<Action> actions =
        new ArrayList<>(
            ImmutableList.of(
                new ActionChoose(choices),
                new ActionNextChoice(choices),
                new ActionPreviousChoice(choices)));
    context.addActions(this, actions);
  }

  public void updateScroll(final Context context) {
    final Pair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final DisplayNode preview = row.first;
    final DisplayNode text = row.second;
    final int converse = preview.converse();
    final int converseEdge = text.converseEdge();
    scroll = Math.min(converse, Math.max(converseEdge - context.edge, scroll));
    tableGroup.setConverse(scroll, context.syntax.animateDetails);
  }

  private void changeChoice(final Context context, final int index) {
    this.index = index;
    final Pair<CourseDisplayNode, CourseDisplayNode> row = rows.get(index);
    final CourseDisplayNode preview = row.first;
    final CourseDisplayNode text = row.second;
    final int converse = preview.converse();
    final int transverse = Math.min(preview.baselineTransverse(), text.baselineTransverse());
    final int converseEdge = text.converseEdge();
    final int transverseEdge =
        Math.max(preview.transverseEdge(), text.transverseEdge());
    highlight.setSize(context, converseEdge - converse, transverseEdge - transverse);
    highlight.setPosition(new Vector(converse, transverse), false);
    updateScroll(context);
  }

  public void destroy(final Context context) {
    context.removeActions(this);
    context.removeConverseEdgeListener(edgeListener);
  }

  @Override
  public void tagsChanged(final Context context) {}

  private class ActionChoose implements Action {
    public String id() {
        return "choose";
    }
    private final List<? extends TwoColumnChoice> choices;

    public ActionChoose(final List<? extends TwoColumnChoice> choices) {
      this.choices = choices;
    }

    @Override
    public void run(final Context context) {
      choices.get(index).choose(context);
      return true;
    }
  }

  private class ActionNextChoice implements Action {
    public String id() {
        return "next_choice";
    }
    private final List<? extends TwoColumnChoice> choices;

    public ActionNextChoice(final List<? extends TwoColumnChoice> choices) {
      this.choices = choices;
    }

    @Override
    public void run(final Context context) {
      changeChoice(context, (index + 1) % choices.size());
      return true;
    }
  }

  private class ActionPreviousChoice implements Action {
    public String id() {
        return "previous_choice";
    }
    private final List<? extends TwoColumnChoice> choices;

    public ActionPreviousChoice(final List<? extends TwoColumnChoice> choices) {
      this.choices = choices;
    }

    @Override
    public void run(final Context context) {
      changeChoice(context, (index + choices.size() - 1) % choices.size());
      return true;
    }
  }
}
