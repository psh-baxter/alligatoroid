package com.zarbosoft.merman.extensions.hotkeys;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.merman.editor.display.derived.TLayout;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.extensions.hotkeys.grammar.Node;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.Comparator;

public class HotkeysExtension {
  private static final Comparator<Pair<Integer, Action>> matchComparator =
      new ChainComparator<Pair<Integer, Action>>().greaterFirst(p -> p.first).build();
  // Settings
  public final TSList<HotkeyRule> rules = new TSList<>();
  public boolean showDetails;

  // State
  private TSMap<String, ROList<Node>> hotkeys = new TSMap<>();
  private boolean freeTyping = true;
  private Grammar hotkeyGrammar;
  private ParseEventSink<Pair<Integer, Action>> hotkeyParse;
  private String hotkeySequence = "";
  private HotkeyDetails hotkeyDetails = null;

  public HotkeysExtension(final Context context) {
    this.showDetails = showDetails;
    context.keyListener = this::handleEvent;
    final Context.TagsListener tagsListener =
        new Context.TagsListener() {
          @Override
          public void tagsChanged(final Context context) {
            update(context);
          }
        };
    context.addSelectionTagsChangeListener(tagsListener);
    context.addGlobalTagsChangeListener(tagsListener);
    context.addActionChangeListener(
        new Context.ActionChangeListener() {
          @Override
          public void actionsAdded(final Context context) {
            update(context);
          }

          @Override
          public void actionsRemoved(final Context context) {
            update(context);
          }
        });
    update(context);
  }

  private void update(final Context context) {
    if (context.cursor == null) return;
    final ROSet<String> tags =
        context.getGlobalTags().mut().addAll(context.cursor.getTags(context)).ro();
    clean(context);
    hotkeys = new TSMap<>();
    freeTyping = true;
    for (final HotkeyRule rule : rules) {
      if (!tags.containsAll(rule.with) || !tags.intersect(rule.without).isEmpty()) continue;
      hotkeys.putAll(rule.hotkeys);
      freeTyping = freeTyping && rule.freeTyping;
    }
    hotkeyGrammar = new Grammar();
    final Union union = new Union();
    for (Action action : context.actions()) {
      if (hotkeys.contains(action.id())) {
        for (final Node hotkey : hotkeys.get(action.id())) {
          union.add(
              new Operator<StackStore>(hotkey.build()) {
                @Override
                protected StackStore process(StackStore store) {
                  final Pair<Integer, Action> out = new Pair<>(store.stackTop(), action);
                  store = store.popVarSingle(e -> {});
                  return store.pushStack(out);
                }
              });
        }
      }
    }
    hotkeyGrammar.add(
        Grammar.DEFAULT_ROOT_KEY, new Sequence().add(StackStore.prepVarStack).add(union));
  }

  public boolean handleEvent(final Context context, final HIDEvent event) {
    if (hotkeyParse == null) {
      hotkeyParse = new ParseBuilder<Pair<Integer, Action>>().grammar(hotkeyGrammar).parse();
    }
    if (hotkeySequence.isEmpty()) hotkeySequence += event.toString();
    else hotkeySequence += ", " + event.toString();
    boolean ok = false;
    if (!hotkeyParse.ended())
      try {
        hotkeyParse = hotkeyParse.push(event, hotkeySequence);
        ok = true;
      } catch (final InvalidStream ignored) {
      }
    if (!ok) {
      clean(context);
      return freeTyping ? false : true;
    }
    if (hotkeyParse.ended()) {
      final Action action =
          hotkeyParse.allResults().stream().sorted(matchComparator).findFirst().get().second;
      clean(context);
      action.run(context);
    } else {
      if (showDetails) {
        if (hotkeyDetails != null) context.details.removePage(context, hotkeyDetails);
        hotkeyDetails = new HotkeyDetails(context);
        context.details.addPage(context, hotkeyDetails);
      }
    }
    return true;
  }

  private void clean(final Context context) {
    hotkeySequence = "";
    hotkeyParse = null;
    if (hotkeyDetails != null) {
      context.details.removePage(context, hotkeyDetails);
      hotkeyDetails = null;
    }
  }

  private class HotkeyDetails extends DetailsPage {
    public HotkeyDetails(final Context context) {
      final Group group = context.display.group();
      this.node = group;
      final TLayout layout = new TLayout(group);

      final Text first = context.display.text();
      final Style firstStyle =
          context.getStyle(
              context
                  .getGlobalTags()
                  .mut()
                  .add(Tags.TAG_PART_DETAILS_PROMPT)
                  .add(Tags.TAG_PART_DETAILS)
                  .ro());
      first.setColor(context, firstStyle.color);
      first.setFont(context, firstStyle.getFont(context));
      first.setText(context, hotkeySequence);
      layout.add(first);

      final Style lineStyle =
          context.getStyle(
              context
                  .getGlobalTags()
                  .mut()
                  .add(Tags.TAG_PART_DETAILS_LINE)
                  .add(Tags.TAG_PART_DETAILS)
                  .ro());
      final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
      for (final com.zarbosoft.pidgoon.State leaf : hotkeyParse.context().leaves) {
        final Action action = leaf.color();
        final Text rule = context.display.text();
        rule.setColor(context, lineStyle.color);
        rule.setFont(context, lineStyle.getFont(context));
        rule.setText(context, hotkeyGrammar.getNode(action.id()).toString());
        final Text name = context.display.text();
        name.setColor(context, lineStyle.color);
        name.setFont(context, lineStyle.getFont(context));
        name.setText(context, action.id());
        table.add(TSList.of(rule, name));
      }
      table.layout(context);
      layout.add(table.group);
      layout.layout(context);
    }

    @Override
    public void tagsChanged(final Context context) {}
  }
}
