package com.zarbosoft.merman;

import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

public class TestLayoutArray {
  @Test
  public void testStatic() {
    new GeneralTestWizard(
            MiscSyntax.syntax,
             new TreeBuilder(MiscSyntax.array)
                .addArray(
                    "value",
                    new TreeBuilder(MiscSyntax.one).build(),
                    new TreeBuilder(MiscSyntax.one).build())
                .build())
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, ", ")
        .checkTextBrick(0, 3, "one")
        .checkTextBrick(0, 4, "]");
  }

  @Test
  public void testStaticNestedArray() {
    int index = 0;
    new GeneralTestWizard(
            MiscSyntax.syntax,
             new TreeBuilder(MiscSyntax.array)
                .addArray("value", new TreeBuilder(MiscSyntax.array).addArray("value").build())
                .build())
        .checkTextBrick(0, index++, "[")
        .checkTextBrick(0, index++, "[")
        .checkSpaceBrick(0, index++)
        .checkTextBrick(0, index++, "]")
        .checkTextBrick(0, index++, "]");
  }
}
