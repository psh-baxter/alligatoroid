package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.Cursor;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.visual.visuals.CursorAtom;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.wall.Bedding;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.Course;
import com.zarbosoft.merman.core.wall.bricks.BrickEmpty;
import com.zarbosoft.merman.core.wall.bricks.BrickImage;
import com.zarbosoft.merman.core.wall.bricks.BrickLine;
import com.zarbosoft.merman.core.wall.bricks.BrickText;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.editorcore.display.MockeryText;
import com.zarbosoft.merman.editorcore.history.Change;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.instanceOf;
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
    inner.editor.context.wall.addBedding(inner.editor.context, new Bedding(10, 7));
    inner.flushIteration();
  }

  public GeneralTestWizard sendText(final String text) {
    inner.editor.context.cursor.handleTyping(inner.editor.context, text);
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard displayWidth(final int size) {
    inner.displayWidth(size);
    return this;
  }

  public GeneralTestWizard displayHeight(final int size) {
    inner.displayHeight(size);
    return this;
  }

  public GeneralTestWizard checkScroll(final int scroll) {
    assertThat((int) inner.editor.context.scroll, equalTo(scroll));
    return this;
  }

  public GeneralTestWizard checkCourse(final int index, final int t, final int te) {
    final Course course = getCourse(index);
    assertThat((int) course.transverseStart, equalTo(t));
    assertThat((int) (course.transverseStart + course.ascent + course.descent), equalTo(te));
    return this;
  }

  public GeneralTestWizard checkTextBrick(
      final int courseIndex, final int brickIndex, final String text) {
    final Brick brick = getBrick(courseIndex, brickIndex);
    if (!(brick instanceof BrickText)) {
      dumpCourses();
      assertThat(brick, instanceOf(BrickText.class));
    }
    if (!((BrickText) brick).text.text().equals(text)) {
      dumpCourses();
      assertThat(((BrickText) brick).text.text(), equalTo(text));
    }
    return this;
  }

  public GeneralTestWizard checkSpaceBrick(final int courseIndex, final int brickIndex) {
    Brick brick = getBrick(courseIndex, brickIndex);
    if (!(brick instanceof BrickEmpty)) {
      dumpCourses();
      assertThat(brick, instanceOf(BrickEmpty.class));
    }
    return this;
  }

  public GeneralTestWizard dumpCourses() {
    ROList<Course> courses = inner.editor.context.wall.children;
    for (int i = 0; i < courses.size(); ++i) {
      Course course = courses.get(i);
      System.out.printf(" %02d  ", i);
      for (int j = 0; j < course.children.size(); ++j) {
        Brick brick = course.children.get(j);
        if (inner.editor.context.wall.cornerstone == brick) System.out.format("*");
        if (brick instanceof BrickText) {
          System.out.printf("%s ", ((MockeryText) ((BrickText) brick).text).text());
        } else if (brick instanceof BrickImage) {
          System.out.printf("\\i ");
        } else if (brick instanceof BrickLine) {
          System.out.printf("\\l ");
        } else if (brick instanceof BrickEmpty) {
          System.out.printf("\\w ");
        } else throw new Assertion();
      }
      if (inner.editor.context.wall.cornerstoneCourse == course) {
        System.out.format(" **");
      }
      System.out.printf("\n");
    }
    System.out.format("\n");
    return this;
  }

  private Course getCourse(final int courseIndex) {
    ROList<Course> courses = inner.editor.context.wall.children;
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
    assertThat((int) getBrick(courseIndex, brickIndex).getConverse(), equalTo(converse));
    return this;
  }

  public GeneralTestWizard run(final Consumer<Editor> r) {
    // assertThat(inner.editor.context.cursor, is(notNullValue()));
    r.accept(inner.editor);
    // assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard select(String... path) {
    Object got = inner.editor.context.syntaxLocate(new SyntaxPath(path));
    if (got instanceof Atom) ((Atom) got).visual.selectIntoAnyChild(inner.editor.context);
    else if (got instanceof Field) ((Field) got).selectInto(inner.editor.context);
    else throw Assertion.format("Invalid path %s", (Object) path);
    return this;
  }

  public GeneralTestWizard actWindow() {
    if (inner.editor.context.cursor instanceof CursorAtom) {
      ((CursorAtom) inner.editor.context.cursor).actionWindow(inner.editor.context);
    } else if (inner.editor.context.cursor instanceof CursorFieldArray) {
      ((CursorFieldArray) inner.editor.context.cursor).actionWindow(inner.editor.context);
    } else throw new Assertion();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actGatherNext() {
    if (inner.editor.context.cursor instanceof CursorFieldArray) {
      ((CursorFieldArray) inner.editor.context.cursor).actionGatherNext(inner.editor.context);
    } else if (inner.editor.context.cursor instanceof CursorFieldPrimitive) {
      ((CursorFieldPrimitive) inner.editor.context.cursor)
          .actionGatherNextGlyph(inner.editor.context);
    } else throw new Assertion();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actGatherPreviousLine() {
    ((CursorFieldPrimitive) inner.editor.context.cursor)
        .actionGatherPreviousLine(inner.editor.context);
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actEnter() {
    if (inner.editor.context.cursor instanceof CursorAtom) {
      ((CursorAtom) inner.editor.context.cursor).actionEnter(inner.editor.context);
    } else if (inner.editor.context.cursor instanceof CursorFieldArray) {
      ((CursorFieldArray) inner.editor.context.cursor).actionEnter(inner.editor.context);
    } else throw new Assertion();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actExit() {
    Cursor cursor = inner.editor.context.cursor;
    if (cursor instanceof CursorAtom) {
      ((CursorAtom) cursor).actionExit(inner.editor.context);
    } else if (cursor instanceof CursorFieldArray) {
      ((CursorFieldArray) cursor).actionExit(inner.editor.context);
    } else if (cursor instanceof CursorFieldPrimitive) {
      ((CursorFieldPrimitive) cursor).actionExit(inner.editor.context);
    } else throw new Assertion();
    assertThat(cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actWindowTowardsCursor() {
    inner.editor.context.actionWindowTowardsCursor();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actWindowTowardsRoot() {
    inner.editor.context.actionWindowTowardsRoot();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard actWindowClear() {
    inner.editor.context.actionClearWindow();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard checkCourseCount(final int i) {
    if (inner.editor.context.wall.children.size() != i) {
      dumpCourses();
      assertThat(inner.editor.context.wall.children.size(), equalTo(i));
    }
    return this;
  }

  public GeneralTestWizard checkTotalBrickCount(final int i) {
    int got = 0;
    for (Course course : inner.editor.context.wall.children) {
      got += course.children.size();
    }
    assertThat(got, equalTo(i));
    return this;
  }

  public GeneralTestWizard sendHIDEvent(final ButtonEvent event) {
    inner.sendHIDEvent(event);
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard checkArrayTree(final Atom... atoms) {
    final FieldArray documentAtoms =
        (FieldArray) inner.editor.context.document.root.namedFields.getOpt("value");
    assertThat(documentAtoms.data.size(), equalTo(atoms.length));
    for (int i = 0; i < atoms.length; ++i) {
      Helper.assertTreeEqual(atoms[i], documentAtoms.data.get(i));
    }
    return this;
  }

  public GeneralTestWizard checkCursorPath(String... segments) {
    assertThat(inner.editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath(segments)));
    return this;
  }

  public GeneralTestWizard editInsertBefore() {
    ((EditCursorFieldArray) inner.editor.context.cursor).editInsertBefore(inner.editor);
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard editInsertAfter() {
    ((EditCursorFieldArray) inner.editor.context.cursor).editInsertAfter(inner.editor);
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard editDelete() {
    if (inner.editor.context.cursor instanceof EditCursorAtom) {
      ((EditCursorAtom) inner.editor.context.cursor).editDelete(inner.editor);
    } else if (inner.editor.context.cursor instanceof CursorFieldArray) {
      ((EditCursorFieldArray) inner.editor.context.cursor).editDelete(inner.editor);
    } else throw new Assertion();
    assertThat(inner.editor.context.cursor, is(notNullValue()));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard resize(int converse) {
    inner.resize(converse);
    return this;
  }

  public GeneralTestWizard change(Change change) {
    inner.editor.history.record(
        inner.editor, null, r -> r.apply(inner.editor, change));
    inner.flushIteration();
    return this;
  }

  public GeneralTestWizard checkBrickNotCompact(int courseIndex, int brickIndex) {
    assertThat(getBrick(courseIndex, brickIndex).getVisual().atomVisual().compact, is(false));
    return this;
  }

  public GeneralTestWizard checkBrickCompact(int courseIndex, int brickIndex) {
    assertThat(getBrick(courseIndex, brickIndex).getVisual().atomVisual().compact, is(true));
    return this;
  }

  public GeneralTestWizard dumpTree() {
    Helper.dumpTree(inner.editor);
    return this;
  }

  /*
  public GeneralTestWizard sendText(final String text) {
  	inner.context.cursor.receiveText(inner.context, text);
  	inner.flushIteration();
  	return this;
  }

  public GeneralTestWizard checkChoices(final int count) {
  	assertThat(choices.size(), equalTo(count));
  	return this;
  }
   */
}
