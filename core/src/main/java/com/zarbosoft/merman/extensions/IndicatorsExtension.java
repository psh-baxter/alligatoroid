package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndicatorsExtension extends Extension {

  public List<Indicator> indicators = new ArrayList<>();
  public boolean converseStart = true;
  public int conversePadding = 0;
  public boolean transverseStart = true;
  public int transversePadding = 0;

  @Override
  public State create(final ExtensionContext context) {
    return new ModuleState(context);
  }

  public static class Indicator {

    public String id;

    public Set<Tag> tags = new HashSet<>();

    public Symbol symbol;

    DisplayNode node;
  }

  private class ModuleState extends State {
    private final Group group;
    private final Context.ContextIntListener resizeListener =
        new Context.ContextIntListener() {
          @Override
          public void changed(final Context context, final int oldValue, final int newValue) {
            updatePosition(context);
          }
        };

    ModuleState(final Context context) {
      final Context.TagsListener listener =
          new Context.TagsListener() {
            @Override
            public void tagsChanged(final Context context) {
              if (context.cursor == null) update(context, context.globalTags);
              else update(context, context.globalTags.plusAll(context.cursor.getTags(context)));
            }
          };
      context.addGlobalTagsChangeListener(listener);
      context.addConverseEdgeListener(resizeListener);
      context.addTransverseEdgeListener(resizeListener);
      group = context.display.group();
      context.midground.add(group);
      update(context, context.globalTags);
      updatePosition(context);
    }

    public void update(final Context context, final PSet<Tag> tags) {
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
              context.getStyle(
                  tags.plus(new TypeTag(indicator.id)).plus(new PartTag("indicator"))));
          final int ascent;
          final int descent;
          if (node instanceof Text) {
            final Font font = ((Text) node).font();
            ascent = font.getAscent();
            descent = font.getDescent();
          } else {
            ascent = 0;
            descent = node.transverseSpan(context);
          }
          transverse += ascent;
          node.setTransverse(context, transverse);
          transverse += descent;
          if (!converseStart) {
            node.setConverse(context, -node.converseSpan(context));
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
          context,
          new Vector(
              converseStart
                  ? conversePadding
                  : (context.edge - conversePadding - group.converseSpan(context)),
              transverseStart ? transversePadding : (context.edge - transversePadding)),
          false);
    }

    @Override
    public void destroy(final ExtensionContext context) {
      context.midground.remove(group);
      context.removeConverseEdgeListener(resizeListener);
      context.removeTransverseEdgeListener(resizeListener);
    }
  }
}
