package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.wall.Attachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestAttachments {
  @Test
  public void testPrimitiveRemoveAttachments() {
    FreeAtomType text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(text)
            .group("any", new GroupBuilder().type(text).build())
            .build();

    final Atom textAtom = new TreeBuilder(text).add("value", "hi\ndog").build();
    final FieldPrimitive value = (FieldPrimitive) textAtom.fields.getOpt("value");
    final Brick[] lastBrick = {null};
    final Attachment listener =
        new Attachment() {
          @Override
          public void destroy(final Context context) {
            lastBrick[0] = textAtom.visual.getLastBrick(context);
            lastBrick[0].addAttachment(context, this);
          }
        };
    new GeneralTestWizard(syntax, textAtom)
        .run(
            editor -> {
              textAtom.visual.getLastBrick(editor.context).addAttachment(editor.context, listener);
            })
        .run(
            editor ->
                editor.history.record(
                    editor.context,
                    null,
                    c -> c.apply(editor.context, new ChangePrimitive(value, 2, 1, ""))))
        .run(
            context -> {
              assertThat(lastBrick[0], equalTo(value.visual.lines.get(0).brick));
            });
  }
}
