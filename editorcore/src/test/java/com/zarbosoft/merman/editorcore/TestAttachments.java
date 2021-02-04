package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.Common;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestAttachments {
    @Test
    public void testPrimitiveRemoveAttachments() {
        FreeAtomType text = new TypeBuilder("text")
                .back(Helper.buildBackDataPrimitive("value"))
                .frontDataPrimitive("value")
                .build();
        Syntax syntax = new SyntaxBuilder("any").type(text).group("any", new GroupBuilder().type(text).build()).build();

        final Atom textAtom = new TreeBuilder(text).add("value", "hi\ndog").build();
        final ValuePrimitive value = (ValuePrimitive) textAtom.fields.getOpt("value");
        final Brick[] lastBrick = {null};
        final Attachment listener = new Attachment() {
            @Override
            public void destroy(final Context context) {
                lastBrick[0] = textAtom.visual.getLastBrick(context);
                lastBrick[0].addAttachment(context, this);
            }
        };
        new GeneralTestWizard(syntax, textAtom).run(context -> {
            textAtom.visual.getLastBrick(context).addAttachment(context, listener);
        }).run(context -> context.history.apply(context, new ChangePrimitiveRemove(value, 2, 1))).run(context -> {
            assertThat(lastBrick[0], equalTo(value.visual.lines.get(0).brick));
        });
    }
}
