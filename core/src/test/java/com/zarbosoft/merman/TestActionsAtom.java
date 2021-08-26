package com.zarbosoft.merman;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsAtom {
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
    ((FieldAtom) context.syntaxLocate(new SyntaxPath("named","value", "0", "named","value"))).selectInto(context);
    Helper.cursorAtom(context).actionEnter(context);
    assertThat(
        context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("named", "value", "0", "named", "value", "named", "value")));
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
    ((FieldAtom) context.syntaxLocate(new SyntaxPath("named","value", "0", "named","value"))).selectInto(context);
    Helper.cursorAtom(context).actionExit(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("named", "value", "0")));
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
        .run(context -> target.fieldParentRef.selectField(context))
        .actNextElement()
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("named", "value", "0", "named", "second"))));
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
        .run(context -> target.fieldParentRef.selectField(context))
        .actPreviousElement()
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("named", "value", "0", "named","first"))));
  }
}
