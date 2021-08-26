package com.zarbosoft.merman;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.ViewerCursorFactory;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.merman.core.visual.visuals.CursorAtom;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.Course;
import com.zarbosoft.merman.core.wall.bricks.BrickEmpty;
import com.zarbosoft.merman.core.wall.bricks.BrickImage;
import com.zarbosoft.merman.core.wall.bricks.BrickLine;
import com.zarbosoft.merman.core.wall.bricks.BrickText;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.editor.display.MockeryText;
import com.zarbosoft.merman.helper.BackRecordBuilder;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TestEnvironment;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

public class TestWindowing {
  public static final FreeAtomType a0_0;
  public static final FreeAtomType a1_0;
  public static final FreeAtomType a2_0;
  public static final FreeAtomType a3_0;
  public static final FreeAtomType a4;
  public static final FreeAtomType a5;
  public static final FreeAtomType a0_1;
  public static final FreeAtomType a1_1;
  public static final FreeAtomType a2_1;
  public static final FreeAtomType a3_1;
  public static final FreeAtomType oneAtom;
  public static final FreeAtomType array;

  static {
    a0_0 = markType("0_0");
    a1_0 = markType("1_0");
    a2_0 = markType("2_0");
    a3_0 = markType("3_0");
    a4 = markType("4");
    a5 = markType("5");
    a0_1 = markType("0_1");
    a1_1 = markType("1_1");
    a2_1 = markType("2_1");
    a3_1 = markType("3_1");
    oneAtom =
        new TypeBuilder("oneAtom")
            .back(
                new BackRecordBuilder()
                    .add("stop", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .depthScore(1)
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .depthScore(1)
            .build();
  }

  public static FreeAtomType markType(String name) {
    return new FreeAtomType(
        new FreeAtomType.Config(
            "a" + name,
            new AtomType.Config(
                name,
                TSList.of(Helper.buildBackPrimitive("a" + name)),
                TSList.of(
                    new FrontSymbolSpec(
                        new FrontSymbolSpec.Config(
                            new SymbolTextSpec(new SymbolTextSpec.Config(name))))))));
  }

  @Test
  public void testInitialNoWindow() {
    int i = 0;
    start(false)
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  public GeneralTestWizard start(final boolean startWindowed) {
    final Syntax out =
        new SyntaxBuilder("any")
            .type(a0_0)
            .type(a1_0)
            .type(a2_0)
            .type(a3_0)
            .type(a4)
            .type(a5)
            .type(a0_1)
            .type(a1_1)
            .type(a2_1)
            .type(a3_1)
            .type(oneAtom)
            .type(array)
            .group(
                "any",
                new GroupBuilder()
                    .type(a0_0)
                    .type(a1_0)
                    .type(a2_0)
                    .type(a3_0)
                    .type(a4)
                    .type(a5)
                    .type(a0_1)
                    .type(a1_1)
                    .type(a2_1)
                    .type(a3_1)
                    .type(oneAtom)
                    .type(array)
                    .build())
            .build();
    final Syntax syntax = out;
    GeneralTestWizard generalTestWizard =
        new GeneralTestWizard(
            syntax,
            startWindowed,
            new TreeBuilder(oneAtom)
                .add(
                    "value",
                    new TreeBuilder(oneAtom).add("value", new TreeBuilder(a0_0).build()).build())
                .build(),
            new TreeBuilder(array)
                .addArray(
                    "value",
                    new TreeBuilder(a1_0).build(),
                    new TreeBuilder(array)
                        .addArray(
                            "value",
                            new TreeBuilder(a2_0).build(),
                            new TreeBuilder(array)
                                .addArray(
                                    "value",
                                    new TreeBuilder(a3_0).build(),
                                    new TreeBuilder(array)
                                        .addArray("value", new TreeBuilder(a4).build())
                                        .build(),
                                    new TreeBuilder(oneAtom)
                                        .add("value", new TreeBuilder(a5).build())
                                        .build(),
                                    new TreeBuilder(a3_1).build())
                                .build(),
                            new TreeBuilder(a2_1).build())
                        .build(),
                    new TreeBuilder(a1_1).build())
                .build(),
            new TreeBuilder(a0_1).build());
    return generalTestWizard;
  }

  @Test
  public void testInitialWindow() {
    int i = 0;
    start(true)
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testWindowArray() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1");
  }

  @Test
  public void testWindowArrayUnselectable() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "2")))
                    .fieldParentRef.selectField(context))
        .checkTextBrick(0, 0, "0_0")
        .actWindow()
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testRewindowArray() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1");
  }

  @Test
  public void testWindowAtom() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((FieldAtom) context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value")))
                    .selectInto(context))
        .actWindow()
        .checkTextBrick(0, i++, "0_0");
  }

  @Test
  public void testWindowAtomUnselectable() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((FieldAtom) context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "named", "value")))
                    .selectInto(context))
        .actWindow()
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testWindowMaxDepth() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, i++, "4");
  }

  @Test
  public void testWindowDown() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindowTowardsCursor()
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1");
  }

  @Test
  public void testWindowDownMaxDepth() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .actWindowTowardsCursor()
        .checkTextBrick(0, i++, "4");
  }

  @Test
  public void testWindowUp() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkCourseCount(1)
        .checkTextBrick(0, 0, "4")
        .actWindowTowardsRoot()
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1");
  }

  /** Moving the window up to the root node shows all root level items */
  @Test
  public void testWindowUpRoot() {
    int i = 0;
    start(true)
        .run(
            context ->
                ((FieldArray) context.syntaxLocate(new SyntaxPath("named", "value")))
                    .selectInto(context, true, 1, 1))
        .actWindow()
        .checkTextBrick(0, 0, "1_0")
        .actWindowTowardsRoot()
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testWindowClear() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .dumpWall()
        .actWindow()
        .dumpWall()
        .actWindowClear()
        .dumpWall()
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testWindowSelectArrayNoChange() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, 0, "1_0")
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1");
  }

  @Test
  public void testWindowSelectArrayEllipsis() {
    int i = 0;
    start(false)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actEnter()
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1");
  }

  @Test
  public void testWindowSelectArrayOutside() {
    int i = 0;
    start(true)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "0")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, 0, "0_0")
        .checkCourseCount(1)
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "3_1")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1");
  }

  @Test
  public void testWindowSelectArrayOutsideRoot() {
    int i = 0;
    start(true)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "0")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, 0, "0_0")
        .checkCourseCount(1)
        .run(
            context ->
                ((Atom) context.syntaxLocate(new SyntaxPath("named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .checkTextBrick(0, i++, "0_0")
        .checkTextBrick(0, i++, "1_0")
        .checkTextBrick(0, i++, "2_0")
        .checkTextBrick(0, i++, "...")
        .checkTextBrick(0, i++, "2_1")
        .checkTextBrick(0, i++, "1_1")
        .checkTextBrick(0, i++, "0_1");
  }

  @Test
  public void testWindowSelectArrayAbove() {
    int i = 0;
    start(true)
        .run(
            context ->
                ((Atom)
                        context.syntaxLocate(
                            new SyntaxPath("named", "value", "1", "named", "value", "1", "named", "value", "1", "named", "value", "1")))
                    .fieldParentRef.selectField(context))
        .actWindow()
        .checkTextBrick(0, 0, "4")
        .checkCourseCount(1)
        .actExit()
        .checkTextBrick(0, i++, "3_0")
        .checkTextBrick(0, i++, "4")
        .checkTextBrick(0, i++, "5")
        .checkTextBrick(0, i++, "3_1");
  }

  public static class GeneralTestWizard {
    public final Context context;

    public GeneralTestWizard(final Syntax syntax, boolean startWindowed, final Atom... atoms) {
      FieldArray rootArrayValue =
          new FieldArray((BaseBackArraySpec) syntax.root.namedFields.get("value"));
      rootArrayValue.initialSet(TSList.of(atoms));
      Atom rootAtom = new Atom(syntax.root);
      rootAtom.initialSet(new TSList<>(), new TSMap<String, Field>().put("value", rootArrayValue));
      final Document doc = new Document(syntax, rootAtom);
      Context.InitialConfig initialConfig =
          new Context.InitialConfig().startWindowed(startWindowed);
      initialConfig.ellipsizeThreshold = 3;
      context =
          new Context(
              initialConfig,
              syntax,
              doc,
              new MockeryDisplay(Direction.RIGHT, Direction.DOWN),
              new TestEnvironment(),
              null,
              new ViewerCursorFactory());
      flushIteration();
    }

    public void flushIteration() {
      context.flushIteration(1000);
      if (!context.iterationQueue.isEmpty()) throw new AssertionError("Too much idle activity");
    }

    private Course getCourse(final int courseIndex) {
      ROList<Course> courses = context.wall.children;
      if (courseIndex >= courses.size()) {
        dumpWall();
        assertThat(courses.size(), greaterThan(courseIndex));
      }
      return courses.get(courseIndex);
    }

    private Brick getBrick(final int courseIndex, final int brickIndex) {
      final Course course = getCourse(courseIndex);
      if (brickIndex >= course.children.size()) {
        dumpWall();
        assertThat(course.children.size(), greaterThan(brickIndex));
      }
      return course.children.get(brickIndex);
    }

    public GeneralTestWizard checkTextBrick(
        final int courseIndex, final int brickIndex, final String text) {
      final Brick brick = getBrick(courseIndex, brickIndex);
      if (!(brick instanceof BrickText)) {
        dumpWall();
        assertThat(brick, instanceOf(BrickText.class));
      }
      if (!((BrickText) brick).text.text().equals(text)) {
        dumpWall();
        assertThat(((BrickText) brick).text.text(), equalTo(text));
      }
      return this;
    }

    public GeneralTestWizard run(final Consumer<Context> r) {
      r.accept(context);
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actWindow() {
      if (context.cursor instanceof CursorAtom) {
        ((CursorAtom) context.cursor).actionWindow(context);
      } else if (context.cursor instanceof CursorFieldArray) {
        ((CursorFieldArray) context.cursor).actionWindow(context);
      } else throw new Assertion();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actEnter() {
      if (context.cursor instanceof CursorAtom) {
        ((CursorAtom) context.cursor).actionEnter(context);
      } else if (context.cursor instanceof CursorFieldArray) {
        ((CursorFieldArray) context.cursor).actionEnter(context);
      } else throw new Assertion();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actExit() {
      if (context.cursor instanceof CursorAtom) {
        ((CursorAtom) context.cursor).actionExit(context);
      } else if (context.cursor instanceof CursorFieldArray) {
        ((CursorFieldArray) context.cursor).actionExit(context);
      } else throw new Assertion();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actWindowTowardsCursor() {
      context.actionWindowTowardsCursor();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actWindowTowardsRoot() {
      context.actionWindowTowardsRoot();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard actWindowClear() {
      context.actionClearWindow();
      assertThat(context.cursor, is(notNullValue()));
      flushIteration();
      return this;
    }

    public GeneralTestWizard dumpWall() {
      ROList<Course> courses = context.wall.children;
      for (int i = 0; i < courses.size(); ++i) {
        Course course = courses.get(i);
        System.out.printf(" %02d  ", i);
        for (int j = 0; j < course.children.size(); ++j) {
          Brick brick = course.children.get(j);
          if (context.wall.cornerstone == brick) System.out.format("*");
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
        if (context.wall.cornerstoneCourse == course) {
          System.out.format(" **");
        }
        System.out.printf("\n");
      }
      System.out.format("\n");
      return this;
    }

    public GeneralTestWizard checkCourseCount(final int i) {
      if (context.wall.children.size() != i) {
        dumpWall();
        assertThat(context.wall.children.size(), equalTo(i));
      }
      return this;
    }
  }
}
