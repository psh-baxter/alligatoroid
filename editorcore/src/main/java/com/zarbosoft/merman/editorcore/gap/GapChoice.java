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
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.primitivepattern.CharacterEvent;
import com.zarbosoft.merman.core.syntax.primitivepattern.ForceEndCharacterEvent;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeAtom;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.model.Leaf;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.function.Consumer;

public class GapChoice extends TwoColumnChoice {
  public final Atom gap;
  public final FreeAtomType type;
  public final int consumePreceding;
  public final ROList<EditGapCursorFieldPrimitive.PrepareAtomField> supplyFillAtoms;
  public final int consumeText;
  public final Leaf incompleteFields;
  private final TSList<Event> glyphs;
  private final FrontSpec followingSpec;
  private final ROList<FrontSpec> keySpecs;
  private ROList fields;

  public GapChoice(
      Atom gap,
      FreeAtomType type,
      int consumePreceding,
      ROList<EditGapCursorFieldPrimitive.PrepareAtomField> supplyFillAtoms,
      TSList<Event> glyphs,
      int consumeText,
      ROList<FieldPrimitive> fields,
      Leaf incompleteFields,
      ROList<FrontSpec> keySpecs,
      FrontSpec followingSpec) {
    this.gap = gap;
    this.type = type;
    this.consumePreceding = consumePreceding;
    this.supplyFillAtoms = supplyFillAtoms;
    this.glyphs = glyphs;
    this.consumeText = consumeText;
    this.fields = fields;
    this.incompleteFields = incompleteFields;
    this.keySpecs = keySpecs;
    this.followingSpec = followingSpec;
  }

  @Override
  public void choose(Editor editor, History.Recorder recorder) {
    Consumer<History.Recorder> apply =
        recorder1 -> {
          TSMap<String, Field> fields = new TSMap<>();
          /// Aggregate typed text parsing results into primitive fields
          // + identify last primitive
          ROList<FieldPrimitive> preFields;
          if (this.fields != null) {
            preFields = this.fields;
          } else {
            final Step<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>> nextStep =
                new Step<>();
            this.incompleteFields.parse(null, nextStep, new ForceEndCharacterEvent());
            preFields = nextStep.completed.get(0).second.value;
          }
          FieldPrimitive lastPrimitive = preFields.lastOpt();
          for (FieldPrimitive field : preFields) {
            fields.put(field.back.id, field);
          }
          // Remainder text
          StringBuilder remainderText = new StringBuilder();
          for (Event event : glyphs.subFrom(consumeText)) {
            remainderText.append(((CharacterEvent) event).value);
          }
          /// Aggregate consumed preceding atoms and remove from prefix
          if (consumePreceding > 0) {
            FieldArray precedingField =
                (FieldArray) gap.fields.get(SuffixGapAtomType.PRECEDING_KEY);
            TSList<Atom> preceding = precedingField.data;
            recorder1.apply(
                editor.context,
                new ChangeArray(
                    precedingField,
                    preceding.size() - consumePreceding,
                    consumePreceding,
                    ROList.empty));
            for (EditGapCursorFieldPrimitive.PrepareAtomField s : supplyFillAtoms) {
              Field field = s.process(editor, recorder1);
              fields.put(field.back().id, field);
            }
          }
          /// Create atom
          Field following = null;
          Atom created = new Atom(type);
          for (String fieldId : type.fields.keys().difference(fields.keys())) {
            Field field =
                editor.createEmptyField(editor.context.syntax, type.fields.get(fieldId), 0);
            fields.put(fieldId, field);
            if (followingSpec != null && followingSpec.fieldId().equals(fieldId)) {
              following = field;
            }
          }
          created.initialSet(fields);
          /// Place and select next focus
          if (remainderText.length() > 0) {
            if (following != null) {
              place(editor, recorder1, created);
              deepSelectInto(editor, recorder1, following);
            } else {
              placeWithSuffixSelect(editor, recorder1, created);
            }
            ((EditGapCursorFieldPrimitive) editor.context.cursor)
                .editHandleTyping(editor, recorder1, remainderText.toString());
          } else if (lastPrimitive != null) {
            place(editor, recorder1, created);
            lastPrimitive.selectInto(editor.context);
          } else if (following != null) {
            place(editor, recorder1, created);
            deepSelectInto(editor, recorder1, following);
          } else {
            placeWithSuffixSelect(editor, recorder1, created);
          }
        };
    if (recorder != null) apply.accept(recorder);
    else editor.history.record(editor.context, null, apply);
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
    FieldArray precedingField = (FieldArray) gap.fields.getOpt(SuffixGapAtomType.PRECEDING_KEY);
    if (precedingField != null) {
      /// In a suffix gap - reuse, adding to preceding and clearing/selecting text field
      recorder.apply(
          editor.context,
          new ChangeArray(precedingField, precedingField.data.size(), 0, TSList.of(created)));
      FieldPrimitive gapText = (FieldPrimitive) gap.fields.get(GapAtomType.PRIMITIVE_KEY);
      recorder.apply(editor.context, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
      gapText.selectInto(editor.context);
    } else {
      /// Wrap in a suffix gap and select text
      Atom wrap = editor.createEmptyGap(editor.context.syntax.suffixGap);
      if (gap.fieldParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.fieldParentRef;
        recorder.apply(
            editor.context, new ChangeArray(parent.field, parent.index, 1, TSList.of(wrap)));
      } else if (gap.fieldParentRef instanceof FieldAtom.Parent) {
        recorder.apply(
            editor.context, new ChangeAtom(((FieldAtom.Parent) gap.fieldParentRef).field, wrap));
      }
      recorder.apply(
          editor.context,
          new ChangeArray(
              (FieldArray) wrap.fields.get(SuffixGapAtomType.PRECEDING_KEY),
              0,
              0,
              TSList.of(created)));
      FieldPrimitive wrapText = (FieldPrimitive) wrap.fields.get(GapAtomType.PRIMITIVE_KEY);
      wrapText.selectInto(editor.context);
    }
  }

  private void place(Editor editor, History.Recorder recorder, Atom created) {
    FieldArray precedingField = (FieldArray) gap.fields.getOpt(SuffixGapAtomType.PRECEDING_KEY);
    if (precedingField != null && precedingField.data.some()) {
      /// Reuse current suffix gap - add to preceding and clear text
      recorder.apply(
          editor.context,
          new ChangeArray(precedingField, precedingField.data.size(), 0, TSList.of(created)));
      FieldPrimitive gapText = (FieldPrimitive) gap.fields.get(GapAtomType.PRIMITIVE_KEY);
      recorder.apply(editor.context, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
    } else {
      /// Replace gap
      if (gap.fieldParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.fieldParentRef;
        recorder.apply(
            editor.context, new ChangeArray(parent.field, parent.index, 1, TSList.of(created)));
      } else if (gap.fieldParentRef instanceof FieldAtom.Parent) {
        recorder.apply(
            editor.context, new ChangeAtom(((FieldAtom.Parent) gap.fieldParentRef).field, created));
      }
    }
  }

  @Override
  public ROPair<CourseDisplayNode, CourseDisplayNode> display(Editor editor) {
    final CourseGroup previewLayout = new CourseGroup(editor.context.display.group());
    previewLayout.setPadding(editor.context, editor.choicePreviewPadding);
    for (final FrontSpec part : keySpecs) {
      final CourseDisplayNode node;
      if (part instanceof FrontSymbolSpec) {
        node = ((FrontSymbolSpec) part).createDisplay(editor.context);
      } else if (part instanceof FrontPrimitiveSpec) {
        node = editor.gapPlaceholderSymbol.createDisplay(editor.context);
      } else throw new DeadCode();
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
}
