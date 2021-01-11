package com.zarbosoft.merman.editorcore.syntaxgap;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.display.derived.RowLayout;
import com.zarbosoft.merman.editor.gap.GapKey;
import com.zarbosoft.merman.editor.gap.TwoColumnChoice;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

public abstract class SyntacticGapChoice extends TwoColumnChoice {
  public final FreeAtomType type;
  public final GapKey key;
  public final Atom gap;

  public SyntacticGapChoice(FreeAtomType type, GapKey key) {
    this.type = type;
    this.key = key;
  }

  @Override
  public Pair<DisplayNode, DisplayNode> display(Context context) {
    final PSet<Tag> tags = context.globalTags;
    final Style.Baked lineStyle =
      context.getStyle(tags.plus(new PartTag("details_choice")).plus(new PartTag("details")));
    final RowLayout previewLayout = new RowLayout(context.display);
    for (final FrontSpec part : key.keyParts) {
      final DisplayNode node;
      if (part instanceof FrontSymbol) {
        node = ((FrontSymbol) part).createDisplay(context);
      } else if (part instanceof FrontPrimitiveSpec) {
        node = context.syntax.gapPlaceholder.createDisplay(context);
        context.syntax.gapPlaceholder.style(context, node, lineStyle);
      } else
        throw new DeadCode();
      previewLayout.add(node);
    }
    final Blank space = context.display.blank();
    space.setConverseSpan(context, 8);
    previewLayout.add(space);
    previewLayout.layout(context);

    final Text text = context.display.text();
    text.setColor(context, lineStyle.color);
    text.setFont(context, lineStyle.getFont(context));
    text.setText(context, type.name());

    return new Pair<>(previewLayout.group, text);
  }
}
