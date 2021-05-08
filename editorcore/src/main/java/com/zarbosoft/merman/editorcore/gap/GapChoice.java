package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Text;
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
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.primitivepattern.CharacterEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.displayderived.RowLayout;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class GapChoice extends TwoColumnChoice {
  public final Atom gap;
  public final FreeAtomType type;
  public final int consumePreceding;
  public final ROList<EditGapCursor.PrepareAtomField> supplyFillAtoms;
  public final int consumeText;
  private final TSList<Event> glyphs;
  private final FrontSpec followingSpec;
  private final ROList<FrontSpec> keySpecs;
  private ROList fields;

  public GapChoice(
      Atom gap,
      FreeAtomType type,
      int consumePreceding,
      ROList<EditGapCursor.PrepareAtomField> supplyFillAtoms,
      TSList<Event> glyphs,
      int consumeText,
      ROList<FieldPrimitive> fields,
      ROList<FrontSpec> keySpecs,
      FrontSpec followingSpec) {
    this.gap = gap;
    this.type = type;
    this.consumePreceding = consumePreceding;
    this.supplyFillAtoms = supplyFillAtoms;
    this.glyphs = glyphs;
    this.consumeText = consumeText;
    this.fields = fields;
    this.keySpecs = keySpecs;
    this.followingSpec = followingSpec;
  }

  @Override
  public void choose(Editor editor) {
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          TSMap<String, Field> fields = new TSMap<>();

          /// Aggregate typed text parsing results into primitive fields
          // + identify last primitive
          ROList<FieldPrimitive> preFields = this.fields;
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
            recorder.apply(
                editor.context,
                new ChangeArray(
                    precedingField,
                    preceding.size() - consumePreceding,
                    consumePreceding,
                    ROList.empty));
            for (EditGapCursor.PrepareAtomField s : supplyFillAtoms) {
              Field field = s.process(editor, recorder);
              fields.put(field.back().id, field);
            }
          }

          /// Create atom
          Field following = null;
          Atom created = new Atom(type);
          for (String fieldId : type.fields.keys().difference(fields.keys())) {
            Field field = editor.createEmptyField(editor.context, type.fields.get(fieldId));
            fields.put(fieldId, field);
            if (followingSpec.fieldId().equals(fieldId)) {
              following = field;
            }
          }
          created.initialSet(fields);

          /// Place and select next focus
          if (remainderText.length() > 0) {
            if (following != null) {
              place(editor, recorder, created);
              following.selectInto(editor.context);
            } else {
              placeWithSuffixSelect(editor, recorder, created);
            }
            FieldPrimitive gapText =
                ((VisualFrontPrimitive.Cursor) editor.context.cursor).visualPrimitive.value;
            recorder.apply(
                editor.context, new ChangePrimitive(gapText, 0, 0, remainderText.toString()));
          } else if (lastPrimitive != null) {
            place(editor, recorder, created);
            lastPrimitive.selectInto(editor.context);
          } else if (following != null) {
            place(editor, recorder, created);
            following.selectInto(editor.context);
          } else {
            placeWithSuffixSelect(editor, recorder, created);
          }
        });
  }

  private void placeWithSuffixSelect(Editor editor, History.Recorder recorder, Atom created) {
    FieldArray precedingField = (FieldArray) gap.fields.getOpt(SuffixGapAtomType.PRECEDING_KEY);
    if (precedingField != null) {
      /// In a suffix gap - reuse, adding to preceding and clearing/selecting text field
      recorder.apply(
          editor.context,
          new ChangeArray(precedingField, precedingField.data.size(), 0, TSList.of(created)));
      FieldPrimitive gapText = (FieldPrimitive) gap.fields.get(GapAtomType.GAP_PRIMITIVE_KEY);
      recorder.apply(editor.context, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
      gapText.selectInto(editor.context);
    } else {
      /// Wrap in a suffix gap and select text
      Atom wrap = editor.createEmptyGap(editor.context.syntax.suffixGap);
      if (gap.valueParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.valueParentRef;
        recorder.apply(
            editor.context, new ChangeArray(parent.value, parent.index, 1, TSList.of(wrap)));
      } else if (gap.valueParentRef instanceof FieldAtom.Parent) {
        recorder.apply(
            editor.context, new ChangeNodeSet(((FieldAtom.Parent) gap.valueParentRef).value, wrap));
      }
      recorder.apply(
          editor.context,
          new ChangeArray(
              (FieldArray) wrap.fields.get(SuffixGapAtomType.PRECEDING_KEY),
              0,
              0,
              TSList.of(created)));
      FieldPrimitive wrapText = (FieldPrimitive) wrap.fields.get(GapAtomType.GAP_PRIMITIVE_KEY);
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
      FieldPrimitive gapText = (FieldPrimitive) gap.fields.get(GapAtomType.GAP_PRIMITIVE_KEY);
      recorder.apply(editor.context, new ChangePrimitive(gapText, 0, gapText.data.length(), ""));
    } else {
      /// Replace gap
      if (gap.valueParentRef instanceof FieldArray.Parent) {
        FieldArray.Parent parent = (FieldArray.Parent) gap.valueParentRef;
        recorder.apply(
            editor.context, new ChangeArray(parent.value, parent.index, 1, TSList.of(created)));
      } else if (gap.valueParentRef instanceof FieldAtom.Parent) {
        recorder.apply(
            editor.context,
            new ChangeNodeSet(((FieldAtom.Parent) gap.valueParentRef).value, created));
      }
    }
  }

  @Override
  public ROPair<CourseDisplayNode, CourseDisplayNode> display(Editor editor) {
    final RowLayout previewLayout = new RowLayout(editor.context.display);
    for (final FrontSpec part : keySpecs) {
      final CourseDisplayNode node;
      if (part instanceof FrontSymbol) {
        node = ((FrontSymbol) part).createDisplay(editor.context);
      } else if (part instanceof FrontPrimitiveSpec) {
        node = editor.gapPlaceholderSymbol.createDisplay(editor.context);
      } else throw new DeadCode();
      previewLayout.add(node);
    }
    final Blank space = editor.context.display.blank();
    space.setConverseSpan(editor.context, editor.choiceDescriptionStyle.spaceBefore);
    previewLayout.add(space);
    previewLayout.layout();

    final Text text = editor.context.display.text();
    text.setColor(editor.context, editor.choiceDescriptionStyle.color);
    text.setFont(editor.context, Context.getFont(editor.context, editor.choiceDescriptionStyle));
    text.setText(editor.context, type.name());

    return new ROPair<CourseDisplayNode, CourseDisplayNode>(previewLayout.group, text);
  }
}
