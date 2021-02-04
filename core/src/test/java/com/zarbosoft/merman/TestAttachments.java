package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.TSSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestAttachments {
	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() {
		return Arrays.asList(new Object[] {false}, new Object[] {true});
	}

	final public static FreeAtomType text;
	final public static Syntax syntax;

	static {
		text = new TypeBuilder("text")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		syntax = new SyntaxBuilder("any").type(text).group("any", new GroupBuilder().type(text).build()).build();
	}

	public TestAttachments(final boolean flipSetOrder) {
		Context.createSet = () -> new TSSet<>(new Set() {
			List<Object> inner = new ArrayList<>();

			@Override
			public int size() {
				return inner.size();
			}

			@Override
			public boolean isEmpty() {
				return inner.isEmpty();
			}

			@Override
			public boolean contains(final Object o) {
				return inner.contains(o);
			}

			@Override
			public Iterator iterator() {
				return inner.iterator();
			}

			@Override
			public Object[] toArray() {
				return inner.toArray();
			}

			@Override
			public Object[] toArray(final Object[] a) {
				return inner.toArray(a);
			}

			@Override
			public boolean add(final Object o) {
				if (contains(o))
					return false;
				if (flipSetOrder)
					inner.add(0, o);
				else
					inner.add(o);
				return true;
			}

			@Override
			public boolean remove(final Object o) {
				if (!contains(o))
					return false;
				inner.remove(o);
				return true;
			}

			@Override
			public boolean containsAll(final Collection c) {
				return inner.containsAll(c);
			}

			@Override
			public boolean addAll(final Collection c) {
				boolean out = false;
				for (final Object o : c)
					out = out || add(o);
				return out;
			}

			@Override
			public boolean retainAll(final Collection c) {
				boolean out = false;
				for (final Object o : new ArrayList<>(c)) {
					if (!c.contains(o)) {
						out = true;
						remove(o);
					}
				}
				return out;
			}

			@Override
			public boolean removeAll(final Collection c) {
				boolean out = false;
				for (final Object o : c) {
					if (contains(o)) {
						out = true;
						remove(o);
					}
				}
				return out;
			}

			@Override
			public void clear() {
				inner.clear();
			}
		});
	}

	@Test
	public void testPrimitiveExpandAttachments() {
		final Atom textAtom = new TreeBuilder(text).add("value", "higgs dogoid").build();
		final ValuePrimitive value = (ValuePrimitive) textAtom.fields.getOpt("value");
		final Brick[] lastBrick = {null};
		final Attachment listener = new Attachment() {
			@Override
			public void destroy(final Context context) {
				lastBrick[0] = textAtom.visual.getLastBrick(context);
				lastBrick[0].addAttachment(context, this);
			}
		};
		new GeneralTestWizard(syntax,  textAtom)
				.displayWidth(60)
				.checkCourseCount(2)
				.run(context -> {
					textAtom.visual.getLastBrick(context).addAttachment(context, listener);
				})
				.displayWidth(100000)
				.checkCourseCount(1)
				.run(context -> assertThat(lastBrick[0], equalTo(value.visual.lines.get(0).brick)));
	}
}
