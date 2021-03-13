package com.zarbosoft.merman.editorcore.editing;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Field;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.document.values.FieldAtom;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomFromArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionDelete;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionMoveAfter;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionMoveBefore;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionPaste;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionDelete;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionPaste;
import com.zarbosoft.merman.editorcore.editing.actions.AtomActionSuffix;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionCut;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionDeleteNext;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionDeletePrevious;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionJoinLines;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionPaste;
import com.zarbosoft.merman.editorcore.editing.actions.PrimitiveActionSplitLines;
import com.zarbosoft.merman.editorcore.editing.gap.GapAtomType;
import com.zarbosoft.merman.editorcore.editing.suffixgap.SuffixGapAtomType;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionSuffix;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;

import java.util.Arrays;
import java.util.Set;

public class EditingExtension {
    public static final SymbolTextSpec DEFAULT_GAP_PLACEHOLDER = new SymbolTextSpec("•", style);
    public static final boolean DEFAULT_PRETTY_SAVE = true;
    public final Symbol gapPlaceholder;
    public final boolean prettySave;
    private final Context context;
    public final History history;
    public GapAtomType gap;
  public SuffixGapAtomType suffixGap;

    public void atomSet(Context context, History history, VisualFrontAtomBase base, Atom value) {
    base.dispatch(
        new VisualFrontAtomBase.VisualNestedDispatcher() {
          @Override
          public void handle(VisualFrontAtomFromArray visual) {
            history.apply(context, new ChangeNodeSet(((VisualFrontAtom) base).value, value));
          }

          @Override
          public void handle(VisualFrontAtom visual) {
            history.apply(
                    context,
                new ChangeArray(
                    ((VisualFrontAtomFromArray) base).value, 0, 1, ImmutableList.of(value)));
          }
        });
  }

  public Atom arrayInsertNewDefault(Context context, History history, FieldArray value, int index) {
    final Set<AtomType> childTypes =
        context.syntax.splayedTypes.get(value.back().elementAtomType());
    final Atom element;
    if (childTypes.size() == 1) element = childTypes.iterator().next().create(context.syntax);
    else element = gap.create();
    history.apply(context, new ChangeArray(value, index, 0, ImmutableList.of(element)));
    return element;
  }

  public void finish() {
      context.syntax.types.add(gap);
      context.syntax.types.add(suffixGap);
      context.syntax
              .splayedTypes
              .entries()
              .forEach(
                      e -> {
                          e.getValue().add(gap);
                          e.getValue().add(suffixGap);
                      });
      context.createArrayDefault =
              (context1, value) -> {
                  return arrayInsertNewDefault(context1, history, value, 0);
              };
      context.addSelectionListener(
              new Context.SelectionListener() {
                  @Override
                  public void cursorChanged(Context context, Cursor cursor0) {
                      context.textListener = null;
                      context.removeActions(this);
                      cursor0.dispatch(
                              new Cursor.Dispatcher() {
                                  @Override
                                  public void handle(VisualFrontArray.ArrayCursor cursor) {
                                      context.addActions(
                                              this,
                                              Arrays.asList(
                                                      new ArrayActionSuffix(EditingExtension.this, cursor),
                                                      new ArrayActionDelete(EditingExtension.this, cursor),
                                                      new ArrayActionCut(EditingExtension.this, cursor),
                                                      new ArrayActionPaste(EditingExtension.this, cursor),
                                                      new ArrayActionMoveBefore(EditingExtension.this, cursor),
                                                      new ArrayActionMoveAfter(EditingExtension.this, cursor),
                                                      new ArrayActionSuffix(EditingExtension.this, cursor)));
                                  }

                                  @Override
                                  public void handle(VisualFrontAtomBase.NestedCursor cursor) {
                                      VisualFrontAtomBase base = ((VisualFrontAtomBase.NestedCursor) cursor0).base;
                                      context.addActions(
                                              this,
                                              Arrays.asList(
                                                      new AtomActionDelete(EditingExtension.this, base),
                                                      new AtomActionCut(EditingExtension.this,  base),
                                                      new AtomActionPaste(EditingExtension.this, base),
                                                      new AtomActionSuffix(EditingExtension.this, base)));
                                  }

                                  @Override
                                  public void handle(VisualFrontPrimitive.PrimitiveCursor cursor) {
                                      class BasePrimitiveTextListener implements Context.TextListener {

                                          @Override
                                          public void handleText(Context context, String text) {
                                              FieldPrimitive value = cursor.visualPrimitive.value;
                                              if (cursor.range.beginOffset != cursor.range.endOffset)
                                                  history.apply(
                                                          context,
                                                          new ChangePrimitiveRemove(
                                                                  value,
                                                                  cursor.range.beginOffset,
                                                                  cursor.range.endOffset - cursor.range.beginOffset));
                                              history.apply(
                                                      context,
                                                      new ChangePrimitiveAdd(value, cursor.range.beginOffset, text));
                                          }
                                      }
                                      if (cursor instanceof VisualFrontPrimitive.PrimitiveCursor) {
                                          class GapTextListener extends BasePrimitiveTextListener {
                                              private TwoColumnChoicePage choicePage;

                                              public void updateGap(final Context context) {
                                                  FieldPrimitive value = cursor.visualPrimitive.value;
                                                  engineState.update(context, value.data.toString());
                                                  if (choicePage != null) {
                                                      context.details.removePage(context, choicePage);
                                                      choicePage.destroy(context);
                                                  }
                                                  ImmutableList.copyOf(context.gapChoiceListeners)
                                                          .forEach(
                                                                  listener -> listener.changed(context, engineState.choices()));
                                                  if (!engineState.choices().isEmpty()) {
                                                      choicePage = new TwoColumnChoicePage(context, engineState.choices());
                                                      context.details.addPage(context, choicePage);
                                                  } else {
                                                      if (choicePage != null) {
                                                          context.details.removePage(context, choicePage);
                                                          choicePage.destroy(context);
                                                          choicePage = null;
                                                      }
                                                  }
                                              }

                                              @Override
                                              public void handleText(Context context, String text) {
                                                  super.handleText(context, text);
                                                  updateGap(context);
                                              }
                                          }
                                          context.textListener = ((BaseGapAtomType)cursor.visualPrimitive.parent.atomVisual().type()).createTextHandler();
                                          GapTextListener listener = new GapTextListener();
                                          listener.updateGap(context);
                                          context.textListener = listener;
                                      } else {
                                          context.textListener =
                                                  new BasePrimitiveTextListener() {
                                                      @Override
                                                      public void handleText(Context context, String text) {
                                                          FieldPrimitive value = cursor.visualPrimitive.value;
                                                          String preview = value.get();
                                                          if (value.middle.matcher != null) {
                                                              preview =
                                                                      preview.substring(0, cursor.range.beginOffset)
                                                                              + text
                                                                              + preview.substring(
                                                                              cursor.range.endOffset, preview.length());
                                                              if (!value.middle.matcher.match(context,preview)) {
                                                                  VisualAtom atomVisual = cursor.visualPrimitive.atomVisual();
                                                                  if (cursor.range.endOffset == value.length()
                                                                          && atomVisual.children.last() == cursor.visualPrimitive) {
                                                                      editNext();
                                    /*
                                      // Delete after implementing editNext
                                    history.finishChange(context);
                                    final Value.Parent<?> parent = atomVisual.atom.parent;
                                    final Atom gap = suffixGap.create(true, atomVisual.atom);
                                    parentReplace(context, history, parent, gap);
                                    gap.fields.getOpt("gap").selectDown(context);
                                    context.textListener.handleText(context, text);
                                       */
                                                                  }
                                                                  return;
                                                              }
                                                          }
                                                          super.handleText(context, text);
                                                      }
                                                  };
                                      }
                                      context.addActions(
                                              this,
                                              Arrays.asList(
                                                      new PrimitiveActionDeletePrevious(EditingExtension.this, cursor),
                                                      new PrimitiveActionDeleteNext(EditingExtension.this, cursor),
                                                      new PrimitiveActionSplitLines(EditingExtension.this, cursor),
                                                      new PrimitiveActionJoinLines(EditingExtension.this, cursor),
                                                      new PrimitiveActionCut(EditingExtension.this, cursor),
                                                      new PrimitiveActionPaste(EditingExtension.this, cursor)));
                                  }
                              });
                  }
              });
  }

  public EditingExtension(
      Context context,
      History history,
      ) {
      this.context = context;
      this.history = history;
  }

  public void arrayParentDelete(FieldArray.ArrayParent parent) {
      history.apply(
              context, new ChangeArray(parent.value, parent.index, 1, ImmutableList.of()));
  }

    public void parentDelete(Field.Parent<?> parent) {
    parent.dispatch(
        new Field.ParentDispatcher() {
          @Override
          public void handle(FieldArray.ArrayParent parent) {
              arrayParentDelete(parent);
          }

          @Override
          public void handle(FieldAtom.NodeParent parent) {
            history.apply(context, new ChangeNodeSet(parent.value, gap.create()));
          }
        });
  }

  public void parentReplace(Field.Parent<?> parent, Atom atom) {
    parent.dispatch(
        new Field.ParentDispatcher() {
          @Override
          public void handle(FieldArray.ArrayParent parent) {
            history.apply(
                    context, new ChangeArray(parent.value, parent.index, 1, ImmutableList.of(atom)));
          }

          @Override
          public void handle(FieldAtom.NodeParent parent) {
            history.apply(context, new ChangeNodeSet(parent.value, atom));
          }
        });
  }
}
