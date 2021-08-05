package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestHistory {
  public static final FreeAtomType one;
  public static final Syntax syntax;
  public static final Consumer<Editor> modify;
  public static final Consumer<Editor> undo;
  public static final Consumer<Editor> redo;

  static {
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .group("any", new GroupBuilder().type(one).build())
            .build();
    modify =
        new Consumer<Editor>() {
          @Override
          public void accept(final Editor editor) {
            editor.history.record(
                editor,
                null,
                r ->
                    r.apply(
                        editor,
                        new ChangeArray(
                            Helper.rootArray(editor.context.document),
                            0,
                            0,
                            TSList.of(new TreeBuilder(one).build()))));
            editor.history.finishChange();
          }
        };
    undo =
        new Consumer<Editor>() {
          @Override
          public void accept(final Editor editor) {
            editor.history.undo(editor);
          }
        };
    redo =
        new Consumer<Editor>() {
          @Override
          public void accept(final Editor editor) {
            editor.history.redo(editor);
          }
        };
  }

  public GeneralTestWizard initializeWithALongNameToForceChainWrapping() {
    return new GeneralTestWizard(syntax).run(context -> context.history.clear());
  }

  @Test
  public void testEmptyClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testChange() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> assertThat(context.history.isModified(), is(true)));
  }

  @Test
  public void testUndoClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(undo)
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testRedoChange() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(undo)
        .run(redo)
        .run(context -> assertThat(context.history.isModified(), is(true)));
  }

  @Test
  public void testChangeClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testClearUndoChanged() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(undo)
        .run(context -> assertThat(context.history.isModified(), is(true)));
  }

  @Test
  public void testClearRedoClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(undo)
        .run(redo)
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testLateChangeClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(modify)
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testLateClearUndoChanged() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(modify)
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(undo)
        .run(context -> assertThat(context.history.isModified(), is(true)));
  }

  @Test
  public void testLateClearRedoClear() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(modify)
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(undo)
        .run(redo)
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testChangeFinishChange() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.finishChange())
        .run(modify)
        .run(undo)
        .checkArrayTree(new TreeBuilder(one).build());
  }

  @Test
  public void testClearModifiedChangeUndo() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.clearModified())
        .run(modify)
        .run(undo)
        .run(context -> assertThat(context.history.isModified(), is(false)));
  }

  @Test
  public void testModifyAfterFinishUndo() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.finishChange())
        .run(modify)
        .run(undo)
        .run(modify)
        .run(undo)
        .checkArrayTree(new TreeBuilder(one).build());
  }

  @Test
  public void testModifyAfterFinishRedo() {
    initializeWithALongNameToForceChainWrapping()
        .run(modify)
        .run(context -> context.history.finishChange())
        .run(undo)
        .run(redo)
        .run(modify)
        .run(undo)
        .checkArrayTree(new TreeBuilder(one).build());
  }
}
