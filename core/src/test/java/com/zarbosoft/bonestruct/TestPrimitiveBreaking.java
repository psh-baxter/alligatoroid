package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.PrimitiveSyntax;
import com.zarbosoft.bonestruct.helper.PrimitiveTestWizard;
import com.zarbosoft.bonestruct.helper.TreeBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestPrimitiveBreaking {

	@Test
	public void testHardLines() {
		new PrimitiveTestWizard("amp dog\npear").check("amp dog", "pear");
	}

	@Test
	public void testNoBreakUnbreak() {
		new PrimitiveTestWizard("amp dog").resize(3000).check("amp dog").resize(2000000).check("amp dog");
	}

	@Test
	public void testBreakOne() {
		new PrimitiveTestWizard("amp\npear digitize").resize(100).check("amp", "pear ", "digitize");
	}

	@Test
	public void testBreakTwo() {
		new PrimitiveTestWizard("amp dog laserasticatellage\npear volume")
				.resize(200)
				.check("amp dog ", "laserasticatellage", "pear volume");
	}

	@Test
	public void testRebreakOne() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(120)
				.check("over three ", "houses ", "rotisserie ", "volume");
	}

	@Test
	public void testRebreakTwo() {
		new PrimitiveTestWizard("over three houses timing\n rotisserie volume")
				.resize(200)
				.check("over three houses ", "timing", " rotisserie volume")
				.resize(160)
				.check("over three ", "houses timing", " rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOne() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(300)
				.check("over three houses rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOneFully() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(1000)
				.check("over three houses rotisserie volume");
	}

	@Test
	public void testUnbreakableOne() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableMid() {
		new PrimitiveTestWizard("123456789").resize(43).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableDynamic() {
		final Atom primitive = new TreeBuilder(PrimitiveSyntax.primitive).add("value", "123").build();
		new GeneralTestWizard(PrimitiveSyntax.syntax, primitive)
				.resize(40)
				.run(context -> primitive.data.get("value").selectDown(context))
				.run(context -> context.selection.receiveText(context, "4"))
				.checkTextBrick(0, 1, "1234")
				.run(context -> context.selection.receiveText(context, "5"))
				.checkTextBrick(0, 1, "1234")
				.checkTextBrick(1, 0, "5")
				.run(context -> context.selection.receiveText(context, "6"))
				.checkTextBrick(0, 1, "1234")
				.checkTextBrick(1, 0, "56");
	}

	@Test
	public void testUnbreakableRebreak() {
		new PrimitiveTestWizard("123456789").resize(60).check("123456", "789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableUnbreak() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9").resize(50).check("12345", "6789");
	}

	@Test
	public void testUnbreakableUnbreakFull() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9").resize(10000).check("123456789");
	}

	@Test
	public void testMultipleAtoms() {
		new GeneralTestWizard(PrimitiveSyntax.syntax,
				new TreeBuilder(PrimitiveSyntax.primitive).add("value", "oret").build(),
				new TreeBuilder(PrimitiveSyntax.primitive).add("value", "nyibhye").build()
		)
				.checkTextBrick(0, 1, "oret")
				.checkTextBrick(0, 3, "nyibhye")
				.resize(50)
				.checkTextBrick(0, 1, "oret")
				.checkTextBrick(1, 1, "nyibh")
				.checkTextBrick(2, 0, "ye");
	}

	@Test
	public void testMultipleAtomsPriorities() {
		new GeneralTestWizard(PrimitiveSyntax.syntax, new TreeBuilder(PrimitiveSyntax.array).addArray("value",
				new TreeBuilder(PrimitiveSyntax.low).add("value", "oret").build(),
				new TreeBuilder(PrimitiveSyntax.high).add("value", "nyibhye").build()
		).build())
				.checkTextBrick(0, 1, "oret")
				.checkTextBrick(0, 2, "nyibhye")
				.resize(90)
				.checkTextBrick(0, 1, "oret")
				.checkTextBrick(0, 2, "nyibh")
				.checkTextBrick(1, 0, "ye");
	}

	@Test
	public void testFiniteBreaking() {
		new GeneralTestWizard(PrimitiveSyntax.syntax,
				new TreeBuilder(PrimitiveSyntax.quoted).add("value", "123456").build()
		)
				.resize(50)
				.checkCourseCount(2);
	}

	@Test
	public void testFiniteBreakLimit() {
		new GeneralTestWizard(PrimitiveSyntax.syntax,
				new TreeBuilder(PrimitiveSyntax.primitive).add("value", "1234").build()
		)
				.resize(30)
				.checkCourseCount(1);
	}

	@Test
	public void testUnbreakCursor() {
		final Atom primitiveAtom = new TreeBuilder(PrimitiveSyntax.quoted).add("value", "12345").build();
		final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.data.get("value");
		new GeneralTestWizard(PrimitiveSyntax.syntax, primitiveAtom)
				.run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 5, 5))
				.resize(50)
				.checkCourseCount(2)
				.checkCourse(0, -20, -10)
				.checkCourse(1, 0, 10)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 3, 2)))
				.checkCourseCount(1)
				.checkCourse(0, -20, -10)
				.run(context -> {
					assertThat(primitive.visual.selection.range.cursor.drawing.transverse(context), is(-23));
					assertThat(primitive.visual.selection.range.cursor.drawing.transverseEdge(context), is(-9));
				});
	}

	@Test
	public void testUnbreakCursorSplit() {
		final Atom primitiveAtom = new TreeBuilder(PrimitiveSyntax.quoted).add("value", "123456").build();
		final ValuePrimitive primitive = (ValuePrimitive) primitiveAtom.data.get("value");
		new GeneralTestWizard(PrimitiveSyntax.syntax,
				new TreeBuilder(PrimitiveSyntax.primitive).add("value", "aaaaa").build(),
				primitiveAtom
		)
				.run(context -> ((ValuePrimitive) primitive).visual.select(context, true, 6, 6))
				.resize(100)
				.checkCourseCount(2)
				.checkCourse(0, -20, -10)
				.checkCourse(1, 0, 10)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 1, 5)))
				.checkCourseCount(1)
				.checkCourse(0, -20, -10)
				.run(context -> {
					assertThat(primitive.visual.selection.range.cursor.drawing.transverse(context), is(-23));
					assertThat(primitive.visual.selection.range.cursor.drawing.transverseEdge(context), is(-9));
				});
	}
}