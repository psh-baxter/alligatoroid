package com.zarbosoft.merman.syntax.front;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.display.derived.Box;
import com.zarbosoft.merman.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.merman.editor.display.derived.RowLayout;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.primitivepattern.Pattern;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.bytes.BytesHelper;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;

public abstract class FrontGapBase extends FrontSpec {
  private BaseBackPrimitiveSpec dataType;

  protected static List<GapKey> gapKeys(
      final Syntax syntax, final FreeAtomType type, final AtomType childType) {
    final List<GapKey> out = new ArrayList<>();
    final Common.Mutable<GapKey> top = new Common.Mutable<>(new GapKey());
    top.value.indexBefore = -1;
    enumerate(type.front().stream())
        .forEach(
            p -> {
              p.second.dispatch(
                  new FrontSpec.DispatchHandler() {
                    @Override
                    public void handle(final FrontSymbol front) {
                      if (front.condition != null && !front.condition.defaultOn()) return;
                      top.value.keyParts.add(front);
                    }

                    @Override
                    public void handle(final FrontArraySpecBase front) {
                      front.prefix.forEach(front2 -> front2.dispatch(this));
                      BaseBackArraySpec backArray =
                          ((BaseBackArraySpec) type.fields.get(front.field()));
                      flush(!isTypeAllowed(backArray.elementAtomType()));
                      front.suffix.forEach(front2 -> front2.dispatch(this));
                    }

                    private void flush(final boolean drop) {
                      if (!top.value.keyParts.isEmpty()) {
                        if (drop) top.value.indexAfter = -1;
                        else top.value.indexAfter = p.first;
                        out.add(top.value);
                        top.value = new GapKey();
                      }
                      if (drop) top.value.indexBefore = -1;
                      else top.value.indexBefore = p.first;
                    }

                    private boolean isTypeAllowed(final String type) {
                      return childType == null
                          || childType == syntax.gap
                          || childType == syntax.prefixGap
                          || childType == syntax.suffixGap
                          || syntax.getLeafTypes(type).stream().anyMatch(t -> t.equals(childType));
                    }

                    @Override
                    public void handle(final FrontDataAtom front) {
                      flush(
                          !isTypeAllowed(((BaseBackAtomSpec) type.fields.get(front.field())).type));
                    }

                    @Override
                    public void handle(final FrontPrimitiveSpec front) {
                      top.value.keyParts.add(front);
                    }

                    @Override
                    public void handle(final FrontGapBase front) {
                      throw new DeadCode();
                    }
                  });
            });
    if (!top.value.keyParts.isEmpty()) {
      top.value.indexAfter = -1;
      out.add(top.value);
    }
    return out;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final PSet<Tag> tags,
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    return new GapVisualPrimitive(context, parent, atom, tags, visualDepth, depthScore);
  }

  @Override
  public void finish(
      List<Object> errors, Path typePath, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(field());
    this.dataType = atomType.getDataPrimitive(errors, typePath, field());
  }

  @Override
  public String field() {
    return "gap";
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  protected abstract List<? extends Choice> process(
      final Context context, final Atom self, final String string, final Common.UserData store);

  protected abstract void deselect(
      Context context, Atom self, String string, Common.UserData userData);

  private abstract static class ActionBase extends Action {
    public static String group() {
      return "gap";
    }
  }

  public abstract static class Choice {

    public abstract void choose(final Context context, final String string);

    public abstract String name();

    /**
     * Lists front parts following (or preceding) the user provided data to preview what and from
     * where will be completed.
     *
     * @return
     */
    public abstract Iterable<? extends FrontSpec> parts();
  }

  protected static class GapKey {
    public int indexBefore;
    public List<FrontSpec> keyParts = new ArrayList<>();
    public int indexAfter;

    public com.zarbosoft.pidgoon.Node matchGrammar(final FreeAtomType type) {
      final Sequence out = new Sequence();
      for (final FrontSpec part : keyParts) {
        if (part instanceof FrontSymbol) {
          String text = ((FrontSymbol) part).gapKey;
          if (((FrontSymbol) part).type instanceof SymbolTextSpec)
            text = ((SymbolTextSpec) ((FrontSymbol) part).type).text;
          out.add(BytesHelper.stringSequence(text));
        } else if (part instanceof FrontPrimitiveSpec) {
          final BaseBackPrimitiveSpec middle =
              (BaseBackPrimitiveSpec) type.fields.get(((FrontPrimitiveSpec) part).field);
          out.add((middle.pattern == null ? Pattern.repeatedAny : middle.pattern).build());
        } else throw new DeadCode();
      }
      return out;
    }

    public ParseResult parse(final Context context, final FreeAtomType type, final String string) {
      final ParseResult out = new ParseResult();
      final Iterator<FrontSpec> frontIterator = keyParts.iterator();

      // Parse string into primitive parts
      final Set<String> filled = new HashSet<>(type.fields.keySet());
      final TSMap<String, Value> data = new TSMap<>();
      int at = 0;
      for (final FrontSpec front : iterable(frontIterator)) {
        final Grammar grammar = new Grammar();
        if (front instanceof FrontSymbol) {
          String text = ((FrontSymbol) front).gapKey;
          if (text.isEmpty() && ((FrontSymbol) front).type instanceof SymbolTextSpec)
            text = ((SymbolTextSpec) ((FrontSymbol) front).type).text;
          grammar.add("root", BytesHelper.stringSequence(text));
        } else if (front instanceof FrontPrimitiveSpec) {
          final BaseBackPrimitiveSpec middle =
              (BaseBackPrimitiveSpec) type.fields.get(((FrontPrimitiveSpec) front).field);
          grammar.add(
              "root", (middle.pattern == null ? Pattern.repeatedAny : middle.pattern).build());
        } else throw new DeadCode();
        final Pair<Parse, Position> longest =
            new ParseBuilder<>()
                .grammar(grammar)
                .longestMatchFromStart(
                    new ByteArrayInputStream(
                        string.substring(at).getBytes(StandardCharsets.UTF_8)));
        if (front instanceof FrontPrimitiveSpec) {
          data.putNew(
              front.field(),
              new ValuePrimitive(
                  ((FrontPrimitiveSpec) front).dataType,
                  string.substring(at, at + (int) longest.second.absolute)));
          filled.remove(front.field());
          out.nextInput = front;
        } else out.nextInput = null;
        at = at + (int) longest.second.absolute;
        if (at >= string.length()) break;
      }
      if (at < string.length()) out.nextInput = null;
      filled.forEach(middle -> data.putNew(middle, type.fields.get(middle).create(context.syntax)));
      out.remainder = string.substring(at);
      out.atom = new Atom(type, data);

      // Look for the next place to enter text
      if (out.nextInput == null)
        for (final FrontSpec part : iterable(frontIterator)) {
          if (!(part instanceof FrontPrimitiveSpec)) continue;
          out.nextInput = part;
          break;
        }

      return out;
    }

    public static class ParseResult {
      public Atom atom;
      public FrontSpec nextInput;
      public String remainder;
    }
  }

  public class GapVisualPrimitive extends VisualPrimitive {
    private final TSMap<String, Value> data;

    public GapVisualPrimitive(
        final Context context,
        final VisualParent parent,
        final Atom atom,
        final PSet<Tag> tags,
        final int visualDepth,
        final int depthScore) {
      super(
          context,
          parent,
          FrontGapBase.this.dataType.get(atom.fields),
          tags.plus(new PartTag("gap"))
              .plusAll(
                  FrontGapBase.this.tags.stream()
                      .map(s -> new FreeTag(s))
                      .collect(Collectors.toSet())),
          visualDepth,
          depthScore);
      this.data = atom.fields;
    }

    @Override
    public void select(
        final Context context,
        final boolean leadFirst,
        final int beginOffset,
        final int endOffset) {
      super.select(context, leadFirst, beginOffset, endOffset);
      if (((GapSelection) selection).self.data.length() > 0) {
        ((GapSelection) selection).updateGap(context);
      }
    }

    @Override
    public PrimitiveSelection createSelection(
        final Context context,
        final boolean leadFirst,
        final int beginOffset,
        final int endOffset) {
      return new GapSelection(context, leadFirst, beginOffset, endOffset);
    }

    public class GapSelection extends PrimitiveSelection {

      private final ValuePrimitive self;
      private final Common.UserData userData = new Common.UserData();
      private GapDetails gapDetails;

      public GapSelection(
          final Context context,
          final boolean leadFirst,
          final int beginOffset,
          final int endOffset) {
        super(context, leadFirst, beginOffset, endOffset);
        self = dataType.get(data);
      }

      @Override
      public void clear(final Context context) {
        super.clear(context);
        deselect(context, self.parent.atom(), self.get(), userData);
        if (gapDetails != null) {
          context.details.removePage(context, gapDetails);
          gapDetails.destroy(context);
          gapDetails = null;
        }
      }

      @Override
      public void receiveText(final Context context, final String text) {
        super.receiveText(context, text);
        updateGap(context);
      }

      public void updateGap(final Context context) {
        if (gapDetails != null) {
          context.details.removePage(context, gapDetails);
          gapDetails.destroy(context);
        }
        final List<? extends Choice> choices =
            process(context, self.parent.atom(), self.get(), userData);
        ImmutableList.copyOf(context.gapChoiceListeners)
            .forEach(listener -> listener.changed(context, choices));
        if (!choices.isEmpty()) {
          gapDetails = new GapDetails(context, choices);
          context.details.addPage(context, gapDetails);
        } else {
          if (gapDetails != null) {
            context.details.removePage(context, gapDetails);
            gapDetails.destroy(context);
            gapDetails = null;
          }
        }
      }

      private class GapDetails extends DetailsPage {
        private final Box highlight;
        private final Group tableGroup;
        private final Context.ContextIntListener edgeListener;
        List<Pair<DisplayNode, DisplayNode>> rows = new ArrayList<>();
        private int index = 0;
        private int scroll = 0;

        public GapDetails(final Context context, final List<? extends Choice> choices) {
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

          final PSet<Tag> tags = context.globalTags;

          BoxStyle.Baked highlightStyle =
              context.getStyle(
                      tags.plus(new PartTag("details_selection")).plus(new PartTag("details")))
                  .box;
          if (highlightStyle == null) highlightStyle = new BoxStyle.Baked();
          highlightStyle.merge(context.syntax.gapChoiceStyle);
          highlight = new Box(context);
          highlight.setStyle(context, highlightStyle);
          group.add(highlight.drawing);

          final ColumnarTableLayout table =
              new ColumnarTableLayout(context, context.syntax.detailSpan);
          tableGroup = table.group;
          group.add(table.group);

          final Style.Baked lineStyle =
              context.getStyle(
                  tags.plus(new PartTag("details_choice")).plus(new PartTag("details")));
          for (final Choice choice : choices) {
            final RowLayout previewLayout = new RowLayout(context.display);
            for (final FrontSpec part : choice.parts()) {
              final DisplayNode node;
              if (part instanceof FrontSymbol) {
                node = ((FrontSymbol) part).createDisplay(context);
              } else if (part instanceof FrontPrimitiveSpec) {
                node = context.syntax.gapPlaceholder.createDisplay(context);
                context.syntax.gapPlaceholder.style(context, node, lineStyle);
              } else throw new DeadCode();
              previewLayout.add(node);
            }
            final Blank space = context.display.blank();
            space.setConverseSpan(context, 8);
            previewLayout.add(space);
            previewLayout.layout(context);

            final Text text = context.display.text();
            text.setColor(context, lineStyle.color);
            text.setFont(context, lineStyle.getFont(context));
            text.setText(context, choice.name());

            rows.add(new Pair<>(previewLayout.group, text));
            table.add(ImmutableList.of(previewLayout.group, text));
          }
          table.layout(context);
          changeChoice(context, 0);
          final List<Action> actions =
              new ArrayList<>(
                  ImmutableList.of(
                      new ActionChoose(choices),
                      new ActionNextChoice(choices),
                      new ActionPreviousChoice(choices)));
          for (int i = 0; i < 10; ++i) {
            final int i2 = i;
            actions.add(new ActionChooseIndex(i2, choices));
          }
          context.addActions(this, actions);
        }

        public void updateScroll(final Context context) {
          final Pair<DisplayNode, DisplayNode> row = rows.get(index);
          final DisplayNode preview = row.first;
          final DisplayNode text = row.second;
          final int converse = preview.converse(context);
          final int converseEdge = text.converseEdge(context);
          scroll = Math.min(converse, Math.max(converseEdge - context.edge, scroll));
          tableGroup.setConverse(context, scroll, context.syntax.animateDetails);
        }

        private void changeChoice(final Context context, final int index) {
          this.index = index;
          final Pair<DisplayNode, DisplayNode> row = rows.get(index);
          final DisplayNode preview = row.first;
          final DisplayNode text = row.second;
          final int converse = preview.converse(context);
          final int transverse = Math.min(preview.transverse(context), text.transverse(context));
          final int converseEdge = text.converseEdge(context);
          final int transverseEdge =
              Math.max(preview.transverseEdge(context), text.transverseEdge(context));
          highlight.setSize(context, converseEdge - converse, transverseEdge - transverse);
          highlight.setPosition(context, new Vector(converse, transverse), false);
          updateScroll(context);
        }

        public void destroy(final Context context) {
          context.removeActions(this);
          context.removeConverseEdgeListener(edgeListener);
        }

        @Override
        public void tagsChanged(final Context context) {}

        @Action.StaticID(id = "choose")
        private class ActionChoose extends ActionBase {
          private final List<? extends Choice> choices;

          public ActionChoose(final List<? extends Choice> choices) {
            this.choices = choices;
          }

          @Override
          public boolean run(final Context context) {
            choices.get(index).choose(context, self.get());
            return true;
          }
        }

        @Action.StaticID(id = "next_choice")
        private class ActionNextChoice extends ActionBase {
          private final List<? extends Choice> choices;

          public ActionNextChoice(final List<? extends Choice> choices) {
            this.choices = choices;
          }

          @Override
          public boolean run(final Context context) {
            changeChoice(context, (index + 1) % choices.size());
            return true;
          }
        }

        @Action.StaticID(id = "previous_choice")
        private class ActionPreviousChoice extends ActionBase {
          private final List<? extends Choice> choices;

          public ActionPreviousChoice(final List<? extends Choice> choices) {
            this.choices = choices;
          }

          @Override
          public boolean run(final Context context) {
            changeChoice(context, (index + choices.size() - 1) % choices.size());
            return true;
          }
        }

        @Action.StaticID(id = "choose_%s (%s = index)")
        private class ActionChooseIndex extends ActionBase {
          private final int i2;
          private final List<? extends Choice> choices;

          public ActionChooseIndex(final int i2, final List<? extends Choice> choices) {
            this.i2 = i2;
            this.choices = choices;
          }

          @Override
          public boolean run(final Context context) {
            if (i2 >= choices.size()) return false;
            choices.get(i2).choose(context, self.get());
            return true;
          }

          @Override
          public String id() {
            return String.format("choose_%s", i2);
          }
        }
      }
    }
  }
}
