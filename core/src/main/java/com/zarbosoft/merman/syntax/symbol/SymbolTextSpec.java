package com.zarbosoft.merman.syntax.symbol;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.syntax.style.Style;

public class SymbolTextSpec extends Symbol {

  public static PartTag partTag = new PartTag("symbol-text");
  public String text;

  public SymbolTextSpec() {}

  public SymbolTextSpec(final String text) {
    this.text = text;
  }

  @Override
  public DisplayNode createDisplay(final Context context) {
    final Text text = context.display.text();
    text.setText(context, this.text);
    return text;
  }

  @Override
  public void style(final Context context, final DisplayNode node, final Style.Baked style) {
    final Text text = (Text) node;
    text.setFont(context, style.getFont(context));
    text.setColor(context, style.color);
  }

  @Override
  public Brick createBrick(final Context context, final BrickInterface inter) {
    final BrickText out = new BrickText(context, inter);
    out.setText(context, this.text);
    return out;
  }

  @Override
  public PartTag partTag() {
    return partTag;
  }
}
