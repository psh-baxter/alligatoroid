package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.display.MockeryDisplay;
import com.zarbosoft.bonestruct.editor.history.History;
import com.zarbosoft.bonestruct.helper.ExpressionSyntax;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.MiscSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.zarbosoft.bonestruct.helper.Helper.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {

	private Context blank() {
		final Atom gap = MiscSyntax.syntax.gap.create();
		final Document doc =
				new Document(MiscSyntax.syntax, new ValueArray(MiscSyntax.syntax.root, Arrays.asList(gap)));
		final Context context = new Context(MiscSyntax.syntax, doc, new MockeryDisplay(), idleTask -> {
		}, new History());
		gap.visual.selectDown(context);
		return context;
	}

	private void innerTestTransform(
			final Syntax syntax, final Supplier<Atom> start, final Consumer<Context> transform, final Atom end
	) {
		final Context context = buildDoc(syntax, start.get());
		transform.accept(context);
		assertTreeEqual(context, end, context.document.top);
		context.history.undo(context);
		assertTreeEqual(context, start.get(), context.document.top);
		context.history.redo(context);
		assertTreeEqual(context, end, context.document.top);
	}

	@Test
	public void syntaxLeafNodes() {
		final Context context = blank();
		assertThat(context.syntax.getLeafTypes("test_group_1").collect(Collectors.toSet()),
				equalTo(ImmutableSet.of(MiscSyntax.infinity, MiscSyntax.one, MiscSyntax.multiback, MiscSyntax.quoted))
		);
	}

	// ========================================================================
	// Decision making and replacement

	@Test
	public void undecided() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "o").build(),
				context.document.top
		);
	}

	@Test
	public void undecidedFull() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		context.selection.receiveText(context, "n");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "one").build(),
				context.document.top
		);
	}

	@Test
	public void immediate() {
		final Context context = blank();
		context.selection.receiveText(context, "i");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.syntax.suffixGap)
						.addArray("value", ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.infinity).build()))
						.add("gap", "")
						.build(),
				context.document.top
		);
	}

	@Test
	public void binaryImmediate() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "!");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.binaryBang)
						.add("first", new Helper.TreeBuilder(MiscSyntax.one))
						.add("second", new Helper.TreeBuilder(MiscSyntax.syntax.gap).add("gap", ""))
						.build(),
				context.document.top
		);
	}

	@Test
	public void binaryUndecided() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "+");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.syntax.suffixGap)
						.addArray("value", ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.one).build()))
						.add("gap", "+")
						.build(),
				context.document.top
		);
	}

	// ========================================================================
	// Unit gap
	@Test
	public void unitContinueInside() {
		final Context context = blank();
		context.selection.receiveText(context, "\"");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.quoted).add("value", "e").build(),
				context.document.top
		);
	}

	@Test
	public void unitContinueInsideArray() {
		final Context context = blank();
		context.selection.receiveText(context, "[");
		context.selection.receiveText(context, "e");
		dump(context.document.top);
		assertTreeEqual(context,
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new TreeBuilder(MiscSyntax.syntax.gap).add("gap", "e").build())
						.build(),
				context.document.top
		);
	}

	// ========================================================================
	// Suffix

	@Test
	public void suffixContinueInside() {
		innerTestTransform(MiscSyntax.syntax,
				() -> MiscSyntax.syntax.suffixGap.create(true, new Helper.TreeBuilder(MiscSyntax.one).build()),
				context -> {
					context.document.top.data.get(0).visual().selectDown(context);
					context.selection.receiveText(context, "?");
					context.selection.receiveText(context, "e");
				},
				new Helper.TreeBuilder(MiscSyntax.syntax.suffixGap).addArray("value",
						ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.waddle)
								.add("first", new Helper.TreeBuilder(MiscSyntax.one))
								.build())
				).add("gap", "e").build()
		);
	}

	// ========================================================================
	// Prefix
	@Test
	public void prefixContinue() {
		innerTestTransform(MiscSyntax.syntax,
				() -> new Helper.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.one).build())
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
					context.selection.receiveText(context, "x");
					context.selection.receiveText(context, "13");
				},
				new Helper.TreeBuilder(MiscSyntax.multiplier)
						.add("value", new Helper.TreeBuilder(MiscSyntax.one))
						.add("text", "13")
						.build()
		);
	}

	@Test
	public void prefixContinueWrap() {
		innerTestTransform(MiscSyntax.syntax,
				() -> new Helper.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.one).build())
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
					context.selection.receiveText(context, "#");
					context.selection.receiveText(context, "e");
				},
				new Helper.TreeBuilder(MiscSyntax.snooze).add("value",
						new Helper.TreeBuilder(MiscSyntax.syntax.prefixGap)
								.add("gap", "e")
								.addArray("value", new Helper.TreeBuilder(MiscSyntax.one).build())
				).build()
		);
	}

	// ========================================================================
	// Suffix raising

	@Test
	public void testRaisePrecedenceLower() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "*");
				},
				new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Helper.TreeBuilder(ExpressionSyntax.multiply)
										.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	@Test
	public void testRaisePrecedenceEqualAfter() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "+");
				},
				new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Helper.TreeBuilder(ExpressionSyntax.plus)
										.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	@Test
	public void testRaisePrecedenceEqualBefore() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.minus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "-");
				},
				new Helper.TreeBuilder(ExpressionSyntax.minus).add("first",
						new Helper.TreeBuilder(ExpressionSyntax.minus)
								.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Helper.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaisePrecedenceGreater() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.multiply)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "+");
				},
				new Helper.TreeBuilder(ExpressionSyntax.plus).add("first",
						new Helper.TreeBuilder(ExpressionSyntax.multiply)
								.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Helper.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaiseSkipDissimilar() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.subscript)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "+");
				},
				new Helper.TreeBuilder(ExpressionSyntax.plus).add("first",
						new Helper.TreeBuilder(ExpressionSyntax.subscript)
								.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Helper.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaiseBounded() {
		innerTestTransform(ExpressionSyntax.syntax,
				() -> new Helper.TreeBuilder(ExpressionSyntax.inclusiveRange)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Helper.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
					context.selection.receiveText(context, "+");
				},
				new Helper.TreeBuilder(ExpressionSyntax.inclusiveRange)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Helper.TreeBuilder(ExpressionSyntax.plus)
										.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	// ========================================================================
	// Deselection removal

	@Test
	public void testDropArrayElement() {
		innerTestTransform(MiscSyntax.syntax,
				() -> new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", MiscSyntax.syntax.gap.create())
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "0"))).selectDown(context);
					((Atom) context.locateShort(new Path("0", "0"))).parent.selectUp(context);
				},
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value").build()
		);
	}

	@Test
	public void testDontDropNodeGap() {
		innerTestTransform(MiscSyntax.syntax,
				() -> new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", MiscSyntax.syntax.gap.create())
						.build(),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "0"))).selectDown(context);
					context.selection.receiveText(context, "urt");
					((ValueArray) context.locateLong(new Path("0"))).visual().selectDown(context);
				},
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new TreeBuilder(MiscSyntax.syntax.gap).add("gap", "urt").build())
						.build()
		);
	}

	@Test
	public void testDontDropOutOfTree() {
		final Context context =
				buildDoc(MiscSyntax.syntax, MiscSyntax.syntax.gap.create(), MiscSyntax.syntax.gap.create());
		((Atom) context.locateShort(new Path("0"))).visual().selectDown(context);
		((Atom) context.locateShort(new Path("1"))).visual().selectDown(context);
		assertThat(context.document.top.data.size(), equalTo(1));
		assertTreeEqual(context, MiscSyntax.syntax.gap.create(), context.document.top);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
		context.history.undo(context);
		assertThat(context.document.top.data.size(), equalTo(2));
		assertTreeEqual(MiscSyntax.syntax.gap.create(), context.document.top.data.get(0));
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("1", "0")));
		context.history.redo(context);
		assertThat(context.document.top.data.size(), equalTo(1));
		assertTreeEqual(MiscSyntax.syntax.gap.create(), context.document.top.data.get(0));
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
	}

	@Test
	public void testDropSuffixValue() {
		innerTestTransform(MiscSyntax.syntax,
				() -> MiscSyntax.syntax.suffixGap.create(true, new TreeBuilder(MiscSyntax.infinity).build()),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
					((Atom) context.locateShort(new Path("0"))).parent.selectUp(context);
				},
				new Helper.TreeBuilder(MiscSyntax.infinity).build()
		);
	}

	@Test
	public void testDropPrefixValue() {
		innerTestTransform(MiscSyntax.syntax,
				() -> MiscSyntax.syntax.prefixGap.create(new TreeBuilder(MiscSyntax.infinity).build()),
				context -> {
					((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
					((Atom) context.locateShort(new Path("0"))).parent.selectUp(context);
				},
				new Helper.TreeBuilder(MiscSyntax.infinity).build()
		);
	}

	// ========================================================================
	// Array gap creation

	@Test
	public void testCreateArrayGap() {
		innerTestTransform(MiscSyntax.syntax,
				() -> new Helper.TreeBuilder(MiscSyntax.array).addArray("value").build(),
				context -> {
					((ValueArray) context.locateLong(new Path("0"))).visual().selectDown(context);
				},
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value", MiscSyntax.syntax.gap.create()).build()
		);
	}
}
