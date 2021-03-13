package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Course;
import com.zarbosoft.merman.editor.wall.bricks.BrickEmpty;
import com.zarbosoft.merman.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.function.Consumer;

import static com.zarbosoft.rendaw.common.Common.iterable;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

public class GeneralTestWizard {
  public TestWizard inner;

  public GeneralTestWizard(final Syntax syntax, final Atom... atoms) {
    inner = new TestWizard(syntax, atoms);
    inner.context.banner.addMessage(inner.context, new BannerMessage() {});

    final Drawing drawing = inner.context.display.drawing();
    drawing.resize(inner.context, new Vector(500, 7));
    final DetailsPage page =
        new DetailsPage() {
          @Override
          public void tagsChanged(final Context context) {}
        };
    page.node = drawing;
    inner.context.details.addPage(inner.context, page);
    inner.runner.flush();
  }

  public GeneralTestWizard resize(final int size) {
    inner.resize(size);
    return this;
  }

  public GeneralTestWizard resizeTransitive(final int size) {
    inner.resizeTransitive(size);
    return this;
  }

  private void checkNode(final DisplayNode node, final int t, final int te) {
    assertThat(node.transverse(), equalTo(t));
    assertThat(node.transverseEdge(), equalTo(te));
  }

  public GeneralTestWizard checkBanner(final int transversePlusAscent, final int transverseEdge) {
    checkNode(inner.context.banner.text, transversePlusAscent, transverseEdge);
    return this;
  }

  public GeneralTestWizard checkDetails(final int t, final int te) {
    checkNode(inner.context.details.current.node, t, te);
    return this;
  }

  public GeneralTestWizard checkScroll(final int scroll) {
    assertThat(inner.context.scroll, equalTo(scroll));
    return this;
  }

  public GeneralTestWizard checkCourse(final int index, final int t, final int te) {
    final Course course = getCourse(index);
    assertThat(course.transverseStart, equalTo(t));
    assertThat(course.transverseStart + course.ascent + course.descent, equalTo(te));
    return this;
  }

  public GeneralTestWizard checkTextBrick(
      final int courseIndex, final int brickIndex, final String text) {
    final Brick brick = getBrick(courseIndex, brickIndex);
    assertThat(brick, instanceOf(BrickText.class));
    assertThat(((BrickText) brick).text.text(), equalTo(text));
    return this;
  }

  public GeneralTestWizard checkSpaceBrick(final int courseIndex, final int brickIndex) {
    assertThat(getBrick(courseIndex, brickIndex), instanceOf(BrickEmpty.class));
    return this;
  }

  private Course getCourse(final int courseIndex) {
    assertThat(inner.context.foreground.children.size(), greaterThan(courseIndex));
    return inner.context.foreground.children.get(courseIndex);
  }

  private Brick getBrick(final int courseIndex, final int brickIndex) {
    final Course course = getCourse(courseIndex);
    assertThat(course.children.size(), greaterThan(brickIndex));
    return course.children.get(brickIndex);
  }

  public GeneralTestWizard checkBrick(
      final int courseIndex, final int brickIndex, final int converse) {
    assertThat(getBrick(courseIndex, brickIndex).getConverse(), equalTo(converse));
    return this;
  }

  public GeneralTestWizard checkBrickNotHasTag(
      final int courseIndex, final int brickIndex, final Tag tag) {
    assertThat(getBrick(courseIndex, brickIndex).getTags(inner.context), not(hasItem(tag)));
    return this;
  }

  public GeneralTestWizard checkBrickHasTag(
      final int courseIndex, final int brickIndex, final Tag tag) {
    assertThat(getBrick(courseIndex, brickIndex).getTags(inner.context), hasItem(tag));
    return this;
  }

  public GeneralTestWizard checkBrickNotCompact(final int courseIndex, final int brickIndex) {
    return checkBrickNotHasTag(courseIndex, brickIndex, new StateTag("compact"));
  }

  public GeneralTestWizard checkBrickCompact(final int courseIndex, final int brickIndex) {
    return checkBrickHasTag(courseIndex, brickIndex, new StateTag("compact"));
  }

  public GeneralTestWizard run(final Consumer<Context> r) {
    r.accept(inner.context);
    assertThat(inner.context.cursor, is(notNullValue()));
    inner.runner.flush();
    return this;
  }

  public GeneralTestWizard act(final String name) {
    for (final Action action : iterable(inner.context.actions())) {
      if (action.id().equals(name)) {
        action.run(inner.context);
        assertThat(inner.context.cursor, is(notNullValue()));
        inner.runner.flush();
        return this;
      }
    }
    throw new AssertionError(String.format("No action named [%s]", name));
  }

  public GeneralTestWizard checkCourseCount(final int i) {
    assertThat(inner.context.foreground.children.size(), equalTo(i));
    return this;
  }

  public GeneralTestWizard checkBrickCount(final int i) {
    assertThat(
        inner.context.foreground.children.stream().mapToInt(course -> course.children.size()).sum(),
        equalTo(i));
    return this;
  }

  public GeneralTestWizard sendHIDEvent(final HIDEvent event) {
    inner.sendHIDEvent(event);
    inner.runner.flush();
    return this;
  }

  public GeneralTestWizard checkArrayTree(final Atom... atoms) {
    final FieldArray documentAtoms =
        (FieldArray) inner.context.document.root.fields.getOpt("value");
    assertThat(documentAtoms.data.size(), equalTo(atoms.length));
    for (int i = 0; i < atoms.length; ++i) {
      Atom first = atoms[i];
      Atom second = documentAtoms.data.get(i);
      Helper.assertTreeEqual(first, second);
    }
    return this;
  }

  /*
  public GeneralTestWizard sendText(final String text) {
  	inner.context.cursor.receiveText(inner.context, text);
  	inner.runner.flush();
  	return this;
  }

  public GeneralTestWizard checkChoices(final int count) {
  	assertThat(choices.size(), equalTo(count));
  	return this;
  }
   */
}
