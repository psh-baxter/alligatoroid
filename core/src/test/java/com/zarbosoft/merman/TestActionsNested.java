package com.zarbosoft.merman;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsNested {

  @Test
  public void testEnter() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.snooze)
                .add(
                    "value",
                    new TreeBuilder(MiscSyntax.snooze)
                        .add("value", new TreeBuilder(MiscSyntax.infinity).build())
                        .build())
                .build());
    ((Atom) context.syntaxLocate(new SyntaxPath("value", "0", "value", "atom")))
        .valueParentRef.selectValue(context);
    Helper.act(context, "enter");
    assertThat(
        context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "atom", "value", "atom")));
  }

  @Test
  public void testExit() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.snooze)
                .add(
                    "value",
                    new TreeBuilder(MiscSyntax.snooze)
                        .add("value", new TreeBuilder(MiscSyntax.infinity).build())
                        .build())
                .build());
    ((Atom) context.syntaxLocate(new SyntaxPath("value", "0", "value", "atom")))
        .valueParentRef.selectValue(context);
    Helper.act(context, "exit");
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0")));
  }

  @Test
  public void testNext() {
    final Atom target = new TreeBuilder(MiscSyntax.one).build();
    new GeneralTestWizard(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.plus)
                .add("first", target)
                .add("second", new TreeBuilder(MiscSyntax.one).build())
                .build())
        .run(context -> target.valueParentRef.selectValue(context))
        .act("next")
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "second", "atom"))));
  }

  @Test
  public void testPrevious() {
    final Atom target = new TreeBuilder(MiscSyntax.one).build();
    new GeneralTestWizard(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.plus)
                .add("first", new TreeBuilder(MiscSyntax.one).build())
                .add("second", target)
                .build())
        .run(context -> target.valueParentRef.selectValue(context))
        .act("previous")
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "first", "atom"))));
  }
}
