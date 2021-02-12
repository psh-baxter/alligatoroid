package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.MockeryText;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Course;
import com.zarbosoft.merman.editor.wall.bricks.BrickImage;
import com.zarbosoft.merman.editor.wall.bricks.BrickLine;
import com.zarbosoft.merman.editor.wall.bricks.BrickSpace;
import com.zarbosoft.merman.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;

import java.util.function.Consumer;

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
    this(syntax, false, atoms);
  }

  public GeneralTestWizard(final Syntax syntax, boolean startWindowed, final Atom... atoms) {
    inner = new TestWizard(syntax, startWindowed, atoms);
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

  public GeneralTestWizard displayWidth(final int size) {
    inner.displayWidth(size);
    return this;
  }

  public GeneralTestWizard displayHeight(final int size) {
    inner.displayHeight(size);
    return this;
  }

  private void checkNode(
      final DisplayNode node, final int c, final int t, final int ce, final int te) {
    assertThat(node.converse(), equalTo(c));
    assertThat(node.transverse(), equalTo(t));
    assertThat(node.converseEdge(), equalTo(ce));
    assertThat(node.transverseEdge(), equalTo(te));
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
    if (!(brick instanceof BrickText)) {
      dumpCourses();
      assertThat(brick, instanceOf(BrickText.class));
    }
    assertThat(((BrickText) brick).text.text(), equalTo(text));
    return this;
  }

  public GeneralTestWizard checkSpaceBrick(final int courseIndex, final int brickIndex) {
    assertThat(getBrick(courseIndex, brickIndex), instanceOf(BrickSpace.class));
    return this;
  }

  public GeneralTestWizard dumpCourses() {
    ROList<Course> courses = inner.context.foreground.children;
    for (int i = 0; i < courses.size(); ++i) {
      Course course = courses.get(i);
      System.out.printf(" %02d  ", i);
      for (int j = 0; j < course.children.size(); ++j) {
        Brick brick = course.children.get(j);
        if (inner.context.foreground.cornerstone == brick) System.out.format("*");
        if (brick instanceof BrickText) {
          System.out.printf("%s ", ((MockeryText) ((BrickText) brick).text).text());
        } else if (brick instanceof BrickImage) {
          System.out.printf("\\i ");
        } else if (brick instanceof BrickLine) {
          System.out.printf("\\l ");
        } else if (brick instanceof BrickSpace) {
          System.out.printf("\\w ");
        } else throw new Assertion();
      }
      if (inner.context.foreground.cornerstoneCourse == course) {
        System.out.format(" **");
      }
      System.out.printf("\n");
    }
    System.out.format("\n");
    return this;
  }

  private Course getCourse(final int courseIndex) {
    ROList<Course> courses = inner.context.foreground.children;
    if (courseIndex >= courses.size()) {
      dumpCourses();
      assertThat(courses.size(), greaterThan(courseIndex));
    }
    return courses.get(courseIndex);
  }

  private Brick getBrick(final int courseIndex, final int brickIndex) {
    final Course course = getCourse(courseIndex);
    if (brickIndex >= course.children.size()) {
      dumpCourses();
      assertThat(course.children.size(), greaterThan(brickIndex));
    }
    return course.children.get(brickIndex);
  }

  public GeneralTestWizard checkBrick(
      final int courseIndex, final int brickIndex, final int converse) {
    assertThat(getBrick(courseIndex, brickIndex).getConverse(), equalTo(converse));
    return this;
  }

  public GeneralTestWizard checkBrickNotHasTag(
      final int courseIndex, final int brickIndex, final String tag) {
    assertThat(getBrick(courseIndex, brickIndex).getTags(inner.context), not(hasItem(tag)));
    return this;
  }

  public GeneralTestWizard checkBrickHasTag(
      final int courseIndex, final int brickIndex, final String tag) {
    assertThat(getBrick(courseIndex, brickIndex).getTags(inner.context), hasItem(tag));
    return this;
  }

  public GeneralTestWizard checkBrickNotCompact(final int courseIndex, final int brickIndex) {
    return checkBrickNotHasTag(courseIndex, brickIndex, Tags.TAG_COMPACT);
  }

  public GeneralTestWizard checkBrickCompact(final int courseIndex, final int brickIndex) {
    return checkBrickHasTag(courseIndex, brickIndex, Tags.TAG_COMPACT);
  }

  public GeneralTestWizard run(final Consumer<Context> r) {
    r.accept(inner.context);
    assertThat(inner.context.cursor, is(notNullValue()));
    inner.runner.flush();
    return this;
  }

  public GeneralTestWizard select(String... path) {
    Object got = inner.context.syntaxLocate(new Path(path));
    if (got instanceof Atom) ((Atom) got).visual.selectAnyChild(inner.context);
    else if (got instanceof Value) ((Value) got).selectInto(inner.context);
    else throw Assertion.format("Invalid path %s", (Object) path);
    return this;
  }

  public GeneralTestWizard act(final String name) {
    for (final Action action : inner.context.actions()) {
      if (action.id().equals(name)) {
        action.run(inner.context);
        assertThat(inner.context.cursor, is(notNullValue()));
        inner.runner.flush();
        return this;
      }
    }
    throw new AssertionError(Format.format("No action named [%s]", name));
  }

  public GeneralTestWizard checkCourseCount(final int i) {
    if (inner.context.foreground.children.size() != i) {
      dumpCourses();
      assertThat(inner.context.foreground.children.size(), equalTo(i));
    }
    return this;
  }

  public GeneralTestWizard checkTotalBrickCount(final int i) {
    int got = 0;
    for (Course course : inner.context.foreground.children) {
      got += course.children.size();
    }
    assertThat(
        got,
        equalTo(i));
    return this;
  }

  public GeneralTestWizard sendHIDEvent(final HIDEvent event) {
    inner.sendHIDEvent(event);
    inner.runner.flush();
    return this;
  }

  public GeneralTestWizard checkArrayTree(final Atom... atoms) {
    final ValueArray documentAtoms =
        (ValueArray) inner.context.document.root.fields.getOpt("value");
    assertThat(documentAtoms.data.size(), equalTo(atoms.length));
    for (int i = 0; i < atoms.length; ++i) {
      Helper.assertTreeEqual(atoms[i], documentAtoms.data.get(i));
    }
    return this;
  }

  public GeneralTestWizard checkCursorPath(String... segments) {
    assertThat(inner.context.cursor.getSyntaxPath(), equalTo(new Path(segments)));
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
