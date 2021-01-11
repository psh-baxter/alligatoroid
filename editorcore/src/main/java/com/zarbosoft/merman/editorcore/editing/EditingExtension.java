package com.zarbosoft.merman.editorcore.editing;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualNested;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedFromArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionDelete;
import com.zarbosoft.merman.editorcore.syntaxgap.actions.ArrayActionInsertAfter;
import com.zarbosoft.merman.editorcore.syntaxgap.actions.ArrayActionInsertBefore;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionMoveAfter;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionMoveBefore;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionPaste;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionDelete;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionPaste;
import com.zarbosoft.merman.editorcore.syntaxgap.actions.AtomActionSuffix;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionDeleteNext;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionDeletePrevious;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionJoinLines;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionPaste;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionSplitLines;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editorcore.syntaxgap.actions.ArrayActionSuffix;
import com.zarbosoft.rendaw.common.Assertion;

import java.util.Arrays;

import static com.zarbosoft.rendaw.common.Common.last;

public class EditingExtension {
  @FunctionalInterface
  public interface AtomSet {
    void set(final Context context, final Atom atom);
  }

  public static void install(Context context, History history) {
    context.createArrayDefault =
        (context1, value) -> {
          return arrayInsertNewDefault(context1, history, value, 0);
        };
    context.addSelectionListener(
        new Context.SelectionListener() {
          @Override
          public void selectionChanged(Context context, Cursor cursor0) {
            context.textListener = null;
            context.removeActions(this);
            if (cursor0 instanceof VisualArray.ArrayCursor) {
              VisualArray.ArrayCursor cursor = (VisualArray.ArrayCursor) cursor0;
              context.addActions(
                  this,
                  Arrays.asList(
                      new ArrayActionDelete(history, cursor),
                      new ArrayActionCut(history, cursor),
                      new ArrayActionPaste(history, cursor),
                      new ArrayActionMoveBefore(history, cursor),
                      new ArrayActionMoveAfter(history, cursor),
                      new ArrayActionSuffix(history, cursor)));
            } else if (cursor0 instanceof VisualNestedBase.NestedCursor) {
              VisualNestedBase base = ((VisualNestedBase.NestedCursor) cursor0).base;
              final AtomSet set;
              if (base instanceof VisualNested) {
                set =
                    (context1, atom) -> {
                      history.apply(context, new ChangeNodeSet(((VisualNested) base).value, atom));
                    };
              } else if (base instanceof VisualNestedFromArray) {
                set =
                    (context1, atom) -> {
                      history.apply(
                          context,
                          new ChangeArray(
                              ((VisualNestedFromArray) base).value, 0, 1, ImmutableList.of(atom)));
                    };
              } else throw new Assertion();
              context.addActions(
                  this,
                  Arrays.asList(
                      new AtomActionDelete(history, set),
                      new AtomActionCut(history, base, set),
                      new AtomActionPaste(history, base, set),
                      new AtomActionSuffix(history, base, set)));
            } else if (cursor0 instanceof VisualPrimitive.PrimitiveCursor) {
              VisualPrimitive.PrimitiveCursor cursor = (VisualPrimitive.PrimitiveCursor) cursor0;
              context.textListener =
                  new Context.TextListener() {
                    @Override
                    public void handleText(Context context, String text) {
                      ValuePrimitive value = cursor.visualPrimitive.value;
                      String preview = value.get();
                      if (value.middle.matcher != null) {
                        preview =
                            preview.substring(0, cursor.range.beginOffset)
                                + text
                                + preview.substring(cursor.range.endOffset, preview.length());
                        if (!value.middle.matcher.match(preview)) {
                          VisualAtom atomVisual = cursor.visualPrimitive.atomVisual();
                          if (cursor.range.endOffset == value.length()
                              && last(atomVisual.children) == cursor.visualPrimitive) {
                            history.finishChange(context);
                            final Value.Parent<?> parent = atomVisual.atom.parent;
                            final Atom gap = context.syntax.suffixGap.create(true, atomVisual.atom);
                            parentReplace(context, history, parent, gap);
                            gap.fields.getOpt("gap").selectDown(context);
                            context.textListener.handleText(context, text);
                          }
                          return;
                        }
                      }
                      if (cursor.range.beginOffset != cursor.range.endOffset)
                        history.apply(
                            context,
                            new ChangePrimitiveRemove(
                                value,
                                cursor.range.beginOffset,
                                cursor.range.endOffset - cursor.range.beginOffset));
                      history.apply(
                          context, new ChangePrimitiveAdd(value, cursor.range.beginOffset, text));
                    }
                  };
              context.addActions(
                  this,
                  Arrays.asList(
                      new PrimitiveActionDeletePrevious(history, cursor),
                      new PrimitiveActionDeleteNext(history, cursor),
                      new PrimitiveActionSplitLines(history, cursor),
                      new PrimitiveActionJoinLines(history, cursor),
                      new PrimitiveActionCut(history, cursor),
                      new PrimitiveActionPaste(history, cursor)));
            } else throw new Assertion();
          }
        });
  }

  public static void parentDelete(Context context, History history, Value.Parent<?> parent) {
    parent.dispatch(
        new Value.ParentDispatcher() {
          @Override
          public void handle(ValueArray.ArrayParent parent) {
            history.apply(
                context, new ChangeArray(parent.value, parent.index, 1, ImmutableList.of()));
          }

          @Override
          public void handle(ValueAtom.NodeParent parent) {
            history.apply(context, new ChangeNodeSet(parent.value, context.syntax.gap.create()));
          }
        });
  }

  public static void parentReplace(
      Context context, History history, Value.Parent<?> parent, Atom atom) {
    parent.dispatch(
        new Value.ParentDispatcher() {
          @Override
          public void handle(ValueArray.ArrayParent parent) {
            history.apply(
                context, new ChangeArray(parent.value, parent.index, 1, ImmutableList.of(atom)));
          }

          @Override
          public void handle(ValueAtom.NodeParent parent) {
            history.apply(context, new ChangeNodeSet(parent.value, atom));
          }
        });
  }
}
