package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.display.derived.CourseGroup;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.primitivepattern.CharacterEvent;
import com.zarbosoft.merman.core.syntax.primitivepattern.ForceEndCharacterEvent;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.model.Leaf;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ReverseIterable;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.function.Consumer;

public class GapChoice extends TwoColumnChoice {
  public final Atom gap;
  public final FreeAtomType type;
  public final int consumePrecedingAtoms;
  public final ROList<EditGapCursorFieldPrimitive.PrepareAtomField> supplyFillAtoms;
  public final int consumeText;
  public final Leaf incompleteKeyParse;
  private final TSList<Event> glyphs;
  private final FrontSpec followingSpec;
  private final ROList<FrontSpec> allKeyFrontSpecs;
  private ROList<ParsedField> completeMatchFields;

  public GapChoice(
      Atom gap,
      FreeAtomType type,
      int consumePrecedingAtoms,
      ROList<EditGapCursorFieldPrimitive.PrepareAtomField> supplyFillAtoms,
      TSList<Event> glyphs,
      int consumeText,
      ROList<ParsedField> completeMatchFields,
      Leaf incompleteKeyParse,
      ROList<FrontSpec> allKeyFrontSpecs,
      FrontSpec followingSpec) {
    this.gap = gap;
    this.type = type;
    this.consumePrecedingAtoms = consumePrecedingAtoms;
    this.supplyFillAtoms = supplyFillAtoms;
    this.glyphs = glyphs;
    this.consumeText = consumeText;
    this.completeMatchFields = completeMatchFields;
    this.incompleteKeyParse = incompleteKeyParse;
    this.allKeyFrontSpecs = allKeyFrontSpecs;
    this.followingSpec = followingSpec;
  }

  /**
   * Creates a new primitive from the next primtive spec following id, or the first if id is null
   *
   * @param fields
   * @param allKeyFrontSpecs
   * @param precedingPrimitiveId
   * @return
   */
  private static FieldPrimitive generateNextEmptyPrimitive(
      TSMap<String, Field> fields,
      ROList<FrontSpec> allKeyFrontSpecs,
      String precedingPrimitiveId) {
    boolean next = precedingPrimitiveId == null;
    for (FrontSpec spec : allKeyFrontSpecs) {
      if (spec instanceof FrontPrimitiveSpec) {
        if (next) {
          FieldPrimitive generated = new FieldPrimitive(((FrontPrimitiveSpec) spec).field, "");
          fields.put(generated.back.id, generated);
          return generated;
        }
        if (((FrontPrimitiveSpec) spec).fieldId.equals(precedingPrimitiveId)) {
          next = true;
        }
      }
    }
    return null;
  }

  @Override
  public void choose(Editor editor, History.Recorder recorder) {
    Consumer<History.Recorder> apply =
        recorder1 -> {
          TSMap<String, Field> namedFields = new TSMap<>();

          /// Aggregate typed text parsing results into primitive fields
          // + identify last primitive
          FieldPrimitive nextIncompletePrimitiveField = null;
          ROList<ParsedField> preFields;
          if (this.completeMatchFields != null) {
            preFields = this.completeMatchFields;
            ParsedField lastField = preFields.lastOpt();
            if (lastField != null) nextIncompletePrimitiveField = lastField.primitive;
          } else {
            final Step<ROPair<PreGapChoice, EscapableResult<ROList<ParsedField>>>> nextStep =
                new Step<>();
            this.incompleteKeyParse.parse(null, nextStep, new ForceEndCharacterEvent());
            preFields = nextStep.completed.get(0).second.value;

            // Here we look really lookingly to find the last incompletely parsed primitive to
            // move the cursor into later.
            ParsedField lastRes = preFields.lastOpt();
            if (lastRes != null) {
              if (lastRes.primitive == null) {
                // Last parsed was symbol
                if (!lastRes.started) {
                  // But the symbol was not started, so the one before it might be an incomplete
                  // primitive
                  if (preFields.size() >= 2) {
                    nextIncompletePrimitiveField = preFields.getRev(1).primitive;
                  }
                } else {
                  // The symbol was started, so next primitive would be incomplete if it exists
                  // But to do that, need to find the preceding primitive if there was one
                  String finishedPrimitiveId = null;
                  for (ParsedField pair : new ReverseIterable<>(preFields)) {
                    if (pair.primitive == null)
                      continue; // penultimate was also a symbol (and therefore must have completed)
                    // Preceding was primitive - since most primitives don't have a maximum length
                    // assume it was still in progress.
                    finishedPrimitiveId = pair.primitive.back.id;
                    break;
                  }
                  nextIncompletePrimitiveField =
                      generateNextEmptyPrimitive(
                          namedFields, allKeyFrontSpecs, finishedPrimitiveId);
                }
              } else {
                if (lastRes.finished) {
                  // Last parsed primitive was completed (walled in by symbol) - find the next
                  // primitive
                  nextIncompletePrimitiveField =
                      generateNextEmptyPrimitive(
                          namedFields, allKeyFrontSpecs, lastRes.primitive.back.id);
                } else {
                  // Last parsed primitive was incomplete
                  nextIncompletePrimitiveField = lastRes.primitive;
                }
              }
            } else {
              // No primitives reached - find the first primitive and use
              nextIncompletePrimitiveField =
                  generateNextEmptyPrimitive(namedFields, allKeyFrontSpecs, null);
            }
          }

          // Add parsed primitive fields
          for (ParsedField field : preFields) {
            if (field.primitive == null) continue;
            namedFields.put(field.primitive.back.id, field.primitive);
          }

          // Remainder text
          StringBuilder remainderText = new StringBuilder();
          for (Event event : glyphs.subFrom(consumeText)) {
            remainderText.append(((CharacterEvent) event).value);
          }

          /// Aggregate consumed preceding atoms and remove from prefix
          if (consumePrecedingAtoms > 0) {
            FieldArray precedingField =
                (FieldArray) gap.namedFields.get(SuffixGapAtomType.PRECEDING_KEY);
            TSList<Atom> preceding = precedingField.data;
            Editor.arrayChange(
                editor,
                recorder1,
                precedingField,
                preceding.size() - consumePrecedingAtoms,
                consumePrecedingAtoms,
                ROList.empty);
            for (EditGapCursorFieldPrimitive.PrepareAtomField s : supplyFillAtoms) {
              Field field = s.process(editor, recorder1);
              namedFields.put(field.back().id, field);
            }
          }

          /// Create atom
          Field atomFieldFollowingEnteredText = null;
          Atom created = new Atom(type);
          for (String fieldId : type.namedFields.keys().difference(namedFields.keys())) {
            Field field = editor.createEmptyField(type.namedFields.get(fieldId));
            namedFields.put(fieldId, field);
            if (followingSpec != null && followingSpec.fieldId().equals(fieldId)) {
              atomFieldFollowingEnteredText = field;
            }
          }

          TSList<Field> unnamedFields = new TSList<>();
          for (BackSpecData field : type.unnamedFields) {
            unnamedFields.add(Editor.createEndEmptyField(editor.fileIds, field));
          }

          created.initialSet(unnamedFields, namedFields);

          /// Place and select next focus
          if (remainderText.length() > 0) {
            if (atomFieldFollowingEnteredText != null) {
              place(editor, recorder1, created);
              deepSelectInto(editor, recorder1, atomFieldFollowingEnteredText);
            } else {
              placeWithSuffixSelect(editor, recorder1, created);
            }
            ((EditGapCursorFieldPrimitive) editor.context.cursor)
                .editHandleTyping(editor, recorder1, remainderText.toString());
          } else if (nextIncompletePrimitiveField != null) {
            place(editor, recorder1, created);
            nextIncompletePrimitiveField.selectInto(editor.context);
          } else if (atomFieldFollowingEnteredText != null) {
            place(editor, recorder1, created);
            deepSelectInto(editor, recorder1, atomFieldFollowingEnteredText);
          } else {
            placeWithSuffixSelect(editor, recorder1, created);
          }
        };
    if (recorder != null) apply.accept(recorder);
    else editor.history.record(editor, null, apply);
  }

  private void deepSelectInto(Editor editor, History.Recorder recorder, Field following) {
    if (following instanceof FieldPrimitive) {
      following.selectInto(editor.context);
    } else if (following instanceof FieldAtom) {
      following.selectInto(editor.context);
      ((FieldAtom) following).data.selectInto(editor.context);
    } else if (following instanceof FieldArray) {
      editor.arrayInsertNewDefault(recorder, (FieldArray) following, 0);
      following.selectInto(editor.context);
    } else throw new Assertion();
  }

  private void placeWithSuffixSelect(Editor editor, History.Recorder recorder, Atom created) {
    FieldArray precedingField =
        (FieldArray) gap.namedFields.getOpt(SuffixGapAtomType.PRECEDING_KEY);
    if (precedingField != null) {
      /// In a suffix gap - reuse, adding to preceding and clearing/selecting text field
      Editor.arrayChange(
          editor, recorder, precedingField, precedingField.data.size(), 0, TSList.of(created));
      FieldPrimitive gapText = (FieldPrimitive) gap.namedFields.get(GapAtomType.PRIMITIVE_KEY);
      recorder.apply(editor, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
      gapText.selectInto(editor.context);
    } else {
      /// Wrap in a suffix gap and select text
      Atom wrap = editor.createEmptyGap(editor.context.syntax.suffixGap);
      if (gap.fieldParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.fieldParentRef;
        Editor.arrayChange(editor, recorder, parent.field, parent.index, 1, TSList.of(wrap));
      } else if (gap.fieldParentRef instanceof FieldAtom.Parent) {
        Editor.atomSet(editor, recorder, ((FieldAtom.Parent) gap.fieldParentRef).field, wrap);
      }
      Editor.arrayChange(
          editor,
          recorder,
          (FieldArray) wrap.namedFields.get(SuffixGapAtomType.PRECEDING_KEY),
          0,
          0,
          TSList.of(created));
      FieldPrimitive wrapText = (FieldPrimitive) wrap.namedFields.get(GapAtomType.PRIMITIVE_KEY);
      wrapText.selectInto(editor.context);
    }
  }

  private void place(Editor editor, History.Recorder recorder, Atom created) {
    FieldArray precedingField =
        (FieldArray) gap.namedFields.getOpt(SuffixGapAtomType.PRECEDING_KEY);
    if (precedingField != null && precedingField.data.some()) {
      /// Reuse current suffix gap - add to preceding and clear text
      Editor.arrayChange(
          editor, recorder, precedingField, precedingField.data.size(), 0, TSList.of(created));
      FieldPrimitive gapText = (FieldPrimitive) gap.namedFields.get(GapAtomType.PRIMITIVE_KEY);
      recorder.apply(editor, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
    } else {
      /// Replace gap
      if (gap.fieldParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.fieldParentRef;
        Editor.arrayChange(editor, recorder, parent.field, parent.index, 1, TSList.of(created));
      } else if (gap.fieldParentRef instanceof FieldAtom.Parent) {
        Editor.atomSet(editor, recorder, ((FieldAtom.Parent) gap.fieldParentRef).field, created);
      }
    }
  }

  @Override
  public ROPair<CourseDisplayNode, CourseDisplayNode> display(Editor editor) {
    final CourseGroup previewLayout = new CourseGroup(editor.context.display.group());
    previewLayout.setPadding(editor.context, editor.choicePreviewPadding);
    TSList<FrontSymbolSpec> spaces = new TSList<>();
    for (final FrontSpec part : allKeyFrontSpecs) {
      final CourseDisplayNode node;
      if (part instanceof FrontSymbolSpec) {
        if (((FrontSymbolSpec) part).type instanceof SymbolSpaceSpec
            || ((FrontSymbolSpec) part).type instanceof SymbolTextSpec
                && ((SymbolTextSpec) ((FrontSymbolSpec) part).type).text.trim().isEmpty()) {
          spaces.add((FrontSymbolSpec) part);
          continue;
        } else {
          node = ((FrontSymbolSpec) part).createDisplay(editor.context);
        }
      } else if (part instanceof FrontPrimitiveSpec) {
        node = editor.gapPlaceholderSymbol.createDisplay(editor.context);
      } else throw new DeadCode();
      for (FrontSymbolSpec space : spaces) {
        previewLayout.add(space.createDisplay(editor.context));
      }
      spaces.clear();
      previewLayout.add(node);
    }

    final Text text = editor.context.display.text();
    text.setBaselineTransverse(0);
    text.setColor(editor.context, editor.choiceDescriptionStyle.color);
    text.setFont(editor.context, Context.getFont(editor.context, editor.choiceDescriptionStyle));
    text.setText(editor.context, type.name());
    CourseGroup textPad = new CourseGroup(editor.context.display.group());
    textPad.setPadding(editor.context, editor.choiceDescriptionStyle.padding);
    textPad.add(text);

    return new ROPair<CourseDisplayNode, CourseDisplayNode>(previewLayout, textPad);
  }

  public static class ParsedField {
    /** Primitive or null if symbol. */
    public final FieldPrimitive primitive;
    /** Whether the primitive/symbol had at least one glyph parsed */
    public final boolean started;
    /** Whether the primitive/symbol was parsed to the end. Last is false, rest are true? */
    public final boolean finished;

    public ParsedField(FieldPrimitive primitive, boolean started, boolean finished) {
      this.primitive = primitive;
      this.started = started;
      this.finished = finished;
    }
  }
}
