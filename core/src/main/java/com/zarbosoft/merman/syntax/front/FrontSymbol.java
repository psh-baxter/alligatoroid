package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualSymbol;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.syntax.symbol.Symbol;

public class FrontSymbol extends FrontSpec {

  public final Symbol type;

  public final ConditionType condition;

  /**
   * When filling a gap:
   * Text to match non-text symbols, or override the text of text symbols
   */
  public final String gapKey;

  public static class Config {
    public final Symbol type;
    public final ConditionType condition;
    public final String gapKey;
    public final ROSet<String> tags;

    public Config(Symbol type, ConditionType condition, String gapKey, ROSet<String> tags) {
      this.type = type;
      this.condition = condition;
      this.gapKey = gapKey;
      this.tags = tags;
    }
  }

  public FrontSymbol(Config config) {
    super(config.tags);
    type = config.type;
    condition = config.condition;
    gapKey = config.gapKey;
  }

  public Visual createVisual(
          final Context context,
          final VisualParent parent,
          final ROMap<String, Alignment> alignments,
          final int visualDepth,
          final int depthScore) {
    return createVisual(context, parent, null, alignments, visualDepth, depthScore);
  }

  @Override
  public Visual createVisual(
          final Context context,
          final VisualParent parent,
          final Atom atom,
          final ROMap<String, Alignment> alignments,
          final int visualDepth,
          final int depthScore) {
    return new VisualSymbol(
        parent,
        this,
        atom == null ? null : condition == null ? null : condition.create(context, atom),
        visualDepth);
  }

  @Override
  public String field() {
    return null;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public DisplayNode createDisplay(final Context context) {
    final DisplayNode out = type.createDisplay(context);
    // TODO should include the AtomType id or atom's tags
    type.style(
        context,
        out,
        context.getStyle(context.getGlobalTags().mut().addAll(tags).add(type.partTag()).ro()));
    return out;
  }
}
