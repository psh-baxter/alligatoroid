package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class PrimitiveTestWizard {
  TestWizard inner;
  private final VisualFrontPrimitive primitive;

  public PrimitiveTestWizard(final String string) {
    inner =
        new TestWizard(
            PrimitiveSyntax.syntax,
                false, new TreeBuilder(PrimitiveSyntax.primitive).add("value", string).build());
    inner.context.retryExpandFactor = 1.05;
    this.primitive =
        ((FieldPrimitive)
                Helper.rootArray(inner.context.document).data.get(0).fields.getOpt("value"))
            .visual;
  }

  public PrimitiveTestWizard check(final String... lines) {
    String[] got = new String[primitive.lines.size()];
    for (int i = 0; i < primitive.lines.size(); ++i) {
      got[i] = primitive.lines.get(i).text;
    }
    assertThat(got, equalTo(lines));
    return this;
  }

  public PrimitiveTestWizard resize(final int size) {
    inner.displayWidth(size);
    return this;
  }

  public PrimitiveTestWizard resizeTransitive(final int size) {
    inner.displayHeight(size);
    return this;
  }
}
