package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.ClipboardEngine;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.IterationTask;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.history.History;
import com.zarbosoft.merman.editorcore.banner.Banner;
import com.zarbosoft.merman.editorcore.details.Details;

import java.util.List;
import java.util.function.Consumer;

/**
 * Invariants and inner workings - Bricks are only created -- outward from the cornerstone when
 * the selection changes -- when the window expands -- when the model changes, at the visual level
 * where the change occurs
 *
 * <p>The whole document is always loaded. Visuals exist for everything in the window. Bricks
 * eventually exist for everything on screen.
 *
 * <p>The selection may be null within a transaction but always exists afterwards. The initial
 * selection is set by default in context.
 *
 * <p>Tags are used: -- In all visuals to style bricks -- In selected visuals for tag listeners
 * (hotkeys, indicators, etc) therefore groups and atomtype visuals (array excepted) don't need
 * tags - only primitive/constants
 */
public class Editor {

  private final Context context;
  public Banner banner;
  public Details details;

  public Editor(
      final Syntax syntax,
      final Document doc,
      final Display display,
      final Consumer<IterationTask> addIteration,
      final Consumer<Integer> flushIteration,
      final History history,
      final ClipboardEngine clipboardEngine) {
    context =
        new Context(syntax, doc, display, addIteration, flushIteration, history, clipboardEngine, true);
    context.history.clear();
    context.addActions(
        this, ImmutableList.of(new ActionUndo(), new ActionRedo(), new ActionClickHovered()));
    context.addApplyScrollListener(() -> {
      banner.setScroll(this, newScroll);
      details.setScroll(this, newScroll);
    });
  }

  public void destroy() {
    context.modules.forEach(p -> p.destroy(context));
    context.banner.destroy();
    context.wall.clear(context);
  }



  public void addActions(final Object key, final List<Action> actions) {
    context.addActions(key, actions);
  }

  private static class ActionUndo implements Action {
    public String id() {
        return "undo";
    }
    @Override
    public void run(final Context context) {
      return context.history.undo(context);
    }
  }

  private static class ActionRedo implements Action {
    public String id() {
        return "redo";
    }
    @Override
    public void run(final Context context) {
      return context.history.redo(context);
    }
  }

  private static class ActionClickHovered implements Action {
    public String id() {
        return "click_hovered";
    }
    @Override
    public void run(final Context context) {
      if (context.hover == null) return false;
      context.hover.select(context);

      return true;
    }
  }
}
