package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.document.values.ValuePrimitive;
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
        ((ValuePrimitive)
                Helper.rootArray(inner.context.document).data.get(0).fields.getOpt("value"))
            .visual;
  }

  public PrimitiveTestWizard check(final String... lines) {
    assertThat(primitive.lines.stream().map(line -> line.text).toArray(), equalTo(lines));
    return this;
  }

  public PrimitiveTestWizard resize(final int size) {
    inner.resize(size);
    return this;
  }

  public PrimitiveTestWizard resizeTransitive(final int size) {
    inner.resizeTransitive(size);
    return this;
  }
}
