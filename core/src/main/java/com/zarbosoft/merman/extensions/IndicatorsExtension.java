package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.ROSetRef;

import java.util.List;

public class IndicatorsExtension {
  private final Group group;
  private final Context.ContextIntListener resizeListener =
      new Context.ContextIntListener() {
        @Override
        public void changed(final Context context, final int oldValue, final int newValue) {
          updatePosition(context);
        }
      };
  private final List<Indicator> indicators;
  private final boolean converseStart;
  private final int conversePadding;
  private final boolean transverseStart;
  private final int transversePadding;

  public static class Indicator {
    public final String id;
    public final ROSet<String> tags;
    public final Symbol symbol;
    private DisplayNode node;

    public Indicator(String id, ROSet<String> tags, Symbol symbol) {
      this.id = id;
      this.tags = tags;
      this.symbol = symbol;
    }
  }

  public IndicatorsExtension(
      Context context,
      List<Indicator> indicators,
      boolean converseStart,
      int conversePadding,
      boolean transverseStart,
      int transversePadding) {
    this.indicators = indicators;
    this.converseStart = converseStart;
    this.conversePadding = conversePadding;
    this.transverseStart = transverseStart;
    this.transversePadding = transversePadding;
    final Context.TagsListener listener =
        new Context.TagsListener() {
          @Override
          public void tagsChanged(final Context context) {
            if (context.cursor == null) update(context, context.getGlobalTags());
            else
              update(
                  context, context.getGlobalTags().mut().addAll(context.cursor.getTags(context)));
          }
        };
    context.addGlobalTagsChangeListener(listener);
    context.addConverseEdgeListener(resizeListener);
    context.addTransverseEdgeListener(resizeListener);
    group = context.display.group();
    context.midground.add(group);
    update(context, context.getGlobalTags());
    updatePosition(context);
  }

  public void update(final Context context, final ROSetRef<String> tags) {
    int transverse = 0;
    int offset = 0;
    for (final Indicator indicator : indicators) {
      if (tags.containsAll(indicator.tags)) {
        DisplayNode node = indicator.node;
        if (node == null) {
          node = indicator.node = indicator.symbol.createDisplay(context);
          group.add(offset, node);
        }
        indicator.symbol.style(
            context,
            node,
            context.getStyle(tags.mut().add(indicator.id).add(Tags.TAG_PART_INDICATOR).ro()));
        final int ascent;
        final int descent;
        if (node instanceof Text) {
          final Font font = ((Text) node).font();
          ascent = font.getAscent();
          descent = font.getDescent();
        } else {
          ascent = 0;
          descent = node.transverseSpan();
        }
        transverse += ascent;
        node.setTransverse(transverse);
        transverse += descent;
        if (!converseStart) {
          node.setConverse(-node.converseSpan());
        }
        offset += 1;
      } else {
        if (indicator.node != null) {
          group.remove(offset);
          indicator.node = null;
        }
      }
    }
  }

  public void updatePosition(final Context context) {
    group.setPosition(
            new Vector(
            converseStart
                ? conversePadding
                : (context.edge - conversePadding - group.converseSpan()),
            transverseStart ? transversePadding : (context.edge - transversePadding)),
        false);
  }
}
