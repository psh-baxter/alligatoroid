package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestPrimitiveBreaking {
    @Test
    public void testUnbreakableDynamic() {
        final Atom primitive = new TreeBuilder(PrimitiveSyntax.primitive).add("value", "123").build();
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitive)
                .resize(40)
                .run(context -> primitive.fields.getOpt("value").selectInto(context))
                .run(context -> context.cursor.receiveText(context, "4"))
                .checkTextBrick(0, 1, "1234")
                .run(context -> context.cursor.receiveText(context, "5"))
                .checkTextBrick(0, 1, "1234")
                .checkTextBrick(1, 0, "5")
                .run(context -> context.cursor.receiveText(context, "6"))
                .checkTextBrick(0, 1, "1234")
                .checkTextBrick(1, 0, "56");
    }

    @Test
    public void testUnbreakCursor() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.quoted).add("value", "12345").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
                .run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 5, 5))
                .resize(50)
                .checkCourseCount(2)
                .checkCourse(0, -10, 0)
                .checkCourse(1, 10, 20)
                .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 3, 2)))
                .checkCourseCount(1)
                .checkCourse(0, -10, 0)
                .run(
                        context -> {
                            assertThat(
                                    primitive.visual.selection.range.cursor.drawing.transverse(), is(-13));
                            assertThat(
                                    primitive.visual.selection.range.cursor.drawing.transverseEdge(), is(1));
                        });
    }

    @Test
    public void testUnbreakCursorSplit() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.quoted).add("value", "123456").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(
                PrimitiveSyntax.syntax,
                new TreeBuilder(PrimitiveSyntax.primitive).add("value", "aaaaa").build(),
                primitiveAtom)
                .run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 6, 6))
                .resize(100)
                .checkCourseCount(2)
                .checkCourse(0, -10, 0)
                .checkCourse(1, 10, 20)
                .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 1, 5)))
                .checkCourseCount(1)
                .checkCourse(0, -10, 0)
                .run(
                        context -> {
                            assertThat(
                                    primitive.visual.selection.range.cursor.drawing.transverse(), is(-13));
                            assertThat(
                                    primitive.visual.selection.range.cursor.drawing.transverseEdge(), is(1));
                        });
    }

    @Test
    public void testUnbreakClear() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.primitive).add("value", "word egg").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
                .resize(40)
                .checkCourseCount(2)
                .run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 0, 0))
                .run(
                        context -> {
                            context.history.apply(context, new ChangePrimitiveRemove(primitive, 0, 8));
                        })
                .checkCourseCount(1)
                .checkTextBrick(0, 1, "");
    }

    @Test
    public void testUnbreakClearEnd() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.primitive).add("value", "gate\nword egg").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
                .resize(40)
                .checkCourseCount(3)
                .run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 8, 8))
                .run(
                        context -> {
                            context.history.apply(context, new ChangePrimitiveRemove(primitive, 5, 8));
                        })
                .checkCourseCount(2)
                .checkTextBrick(1, 0, "");
    }

    @Test
    public void testUnbreakClearStart() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.primitive).add("value", "word egg\nroad").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
                .resize(40)
                .checkCourseCount(3)
                .run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 0, 0))
                .run(
                        context -> {
                            context.history.apply(context, new ChangePrimitiveRemove(primitive, 0, 8));
                        })
                .checkCourseCount(2)
                .checkTextBrick(0, 1, "");
    }

    @Test
    public void testAddThenSplit() {
        final Atom primitiveAtom =
                new TreeBuilder(PrimitiveSyntax.primitive).add("value", "ab").build();
        final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.fields.getOpt("value");
        new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
                .resize(40)
                .checkCourseCount(1)
                .run(context -> primitive.visual.select(context, true, 0, 2))
                .run(
                        context -> {
                            context.history.apply(context, new ChangePrimitiveAdd(primitive, 1, "ord eg"));
                            primitive.visual.select(context, true, 0, 8);
                        })
                .checkCourseCount(2)
                .checkTextBrick(0, 1, "aord")
                .checkTextBrick(1, 0, " egb");
    }
}
