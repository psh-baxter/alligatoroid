package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.BaseEditPrimitiveCursor;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeNodeSet;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.HomogenousSequence;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class EditGapCursor extends BaseEditPrimitiveCursor {
  public static Reference.Key<ROList<PrepareAtomField>> PRECEDING_ROOT_KEY = new Reference.Key<>();
  public static Reference.Key<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>
      GAP_ROOT_KEY = new Reference.Key<>();
  public final Grammar grammar;
  public String currentText;
  public TwoColumnChoicePage choicePage;

  public EditGapCursor(
      Editor editor,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(editor.context, visualPrimitive, leadFirst, beginOffset, endOffset);
    grammar = createChoiceGrammar(editor.context);
    textChanged(editor, visualPrimitive.value.data.toString());
  }

  @Override
  public void handleTyping(Context context, String text) {
    FieldPrimitive value = visualPrimitive.value;
    String preview = value.get();
    preview =
        preview.substring(0, range.beginOffset)
            + text
            + preview.substring(range.endOffset, preview.length());
    if (textChanged(Editor.get(context), preview)) {
      return;
    }
    super.handleTyping(context, text);
  }

  private Grammar createChoiceGrammar(Context context) {
    // Walk all directly and indirectly reachable types, creating a choice for each
    Atom gap = gapAtom();
    AtomType gapType = gap.type;
    String baseType = gap.valueParentRef.valueType();
    Union<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>> union = new Union<>();

    TSSet<AtomType> seen = new TSSet<>();
    Deque<Iterator<AtomType>> stack = new ArrayDeque<>();
    stack.add(context.syntax.splayedTypes.get(baseType).iterator());
    while (!stack.isEmpty()) {
      Iterator<AtomType> top = stack.peekLast();
      AtomType leafType = top.next();
      if (!top.hasNext()) {
        stack.removeLast();
      }
      if (!seen.addNew(leafType)) continue;

      CandidateInfo info = CandidateInfo.inspect(context.env, leafType);

      if (gapType == context.syntax.gap) {
        if (info.preceding.none()) {
          /// Starts with text, is candidate
          PreGapChoice preChoice =
              new PreGapChoice(
                  (FreeAtomType) leafType, 0, TSList.empty, info.keySpecs, info.following);
          union.add(
              new Color<>(
                  preChoice,
                  new Operator<
                      EscapableResult<ROList<FieldPrimitive>>,
                      ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>(
                      info.keyGrammar) {
                    @Override
                    protected ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>> process(
                        EscapableResult<ROList<FieldPrimitive>> value) {
                      return new ROPair<>(preChoice, value);
                    }
                  }));
        } else {
          /// Suffix of some other atom type - skip and recurse into prefixes
          FrontSpec firstPreceding = info.preceding.get(0);
          String elementType;
          if (firstPreceding instanceof FrontAtomSpec) {
            elementType = ((FrontAtomSpec) firstPreceding).field().type;
          } else if (firstPreceding instanceof FrontArraySpecBase) {
            elementType = ((FrontArraySpecBase) firstPreceding).field.elementAtomType();
          } else throw new Assertion();
          stack.add(context.syntax.splayedTypes.get(elementType).iterator());
        }

      } else if (gapType == context.syntax.suffixGap) {
        /// Check if all front slots before gap match preceding atoms, if so this is a candidate
        // Match reversed slots against reversed preceding atoms (match from ends)
        do {
          HomogenousSequence<PrepareAtomField> precedingConsumptionGrammar =
              new HomogenousSequence<>();
          {
            TSList<FrontSpec> reversePreceding = info.preceding.mut();
            reversePreceding.reverse();
            for (FrontSpec front : reversePreceding) {
              if (front instanceof FrontArraySpecBase) {
                precedingConsumptionGrammar.add(
                    new Operator<ROList<Atom>, PrepareAtomField>(
                        new Repeat<Atom>(
                            new AtomTypeMatch(
                                context.syntax.splayedTypes.get(
                                    ((FrontArraySpecBase) front).field().elementAtomType())))) {
                      @Override
                      protected PrepareAtomField process(ROList<Atom> value) {
                        return new PrepareAtomField() {
                          @Override
                          public Field process(Editor editor, History.Recorder recorder) {
                            FieldArray field = new FieldArray(((FrontArraySpecBase) front).field);
                            recorder.apply(editor.context, new ChangeArray(field, 0, 0, value));
                            return field;
                          }
                        };
                      }
                    });
              } else if (front instanceof FrontAtomSpec) {
                precedingConsumptionGrammar.add(
                    new Operator<Atom, PrepareAtomField>(
                        new AtomTypeMatch(
                            context.syntax.splayedTypes.get(
                                ((FrontAtomSpec) front).field().type))) {
                      @Override
                      protected PrepareAtomField process(Atom value) {
                        return new PrepareAtomField() {
                          @Override
                          public Field process(Editor editor, History.Recorder recorder) {
                            FieldAtom field = new FieldAtom(((FrontAtomSpec) front).field());
                            recorder.apply(editor.context, new ChangeNodeSet(field, value));
                            return field;
                          }
                        };
                      }
                    });
              } else throw new Assertion();
            }
          }

          Grammar precedingPlacementsGrammar =
              new Grammar().add(PRECEDING_ROOT_KEY, precedingConsumptionGrammar);

          FieldArray precedingValues = (FieldArray) gap.fields.get(SuffixGapAtomType.PRECEDING_KEY);
          TSList<Atom> reversePrecedingValues = precedingValues.data.mut();
          reversePrecedingValues.reverse();
          TSList<Event> events = new TSList<Event>();
          for (Atom atom : reversePrecedingValues) {
            events.add(new AtomEvent(atom));
          }
          Pair<Step<ROList<PrepareAtomField>>, Position> longestMatch;
          try {
            longestMatch =
                new ParseBuilder<>(PRECEDING_ROOT_KEY)
                    .grammar(precedingPlacementsGrammar)
                    .uncertainty(100)
                    .longestMatchFromStart(events);
          } catch (InvalidStream | GrammarTooUncertain ignored) {
            break;
          }
          if ((Integer) longestMatch.second.at == -1) {
            break;
          }

          /// Build pre-choice
          PreGapChoice preChoice =
              new PreGapChoice(
                  (FreeAtomType) leafType,
                  (Integer) longestMatch.second.at + 1,
                  longestMatch.first.completed.get(0),
                  info.keySpecs,
                  info.following);
          union.add(
              new Color<>(
                  preChoice,
                  new Operator<
                      EscapableResult<ROList<FieldPrimitive>>,
                      ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>(
                      info.keyGrammar) {
                    @Override
                    protected ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>> process(
                        EscapableResult<ROList<FieldPrimitive>> value) {
                      return new ROPair<>(preChoice, value);
                    }
                  }));
        } while (false);

        /// Descend into slots preceding key
        for (FrontSpec spec : info.preceding) {
          if (spec instanceof FrontArraySpecBase) {
            stack.addLast(
                context
                    .syntax
                    .splayedTypes
                    .get(((FrontArraySpecBase) spec).field().elementAtomType())
                    .iterator());
          } else if (spec instanceof FrontAtomSpec) {
            stack.addLast(
                context.syntax.splayedTypes.get(((FrontAtomSpec) spec).field().type).iterator());
          } else throw new Assertion();
        }
      } else throw new Assertion();
    }
    return new Grammar().add(GAP_ROOT_KEY, union);
  }

  private Atom gapAtom() {
    return visualPrimitive.value.atomParentRef.atom();
  }

  @Override
  public void destroy(Context context) {
    if (choicePage != null) {
      Editor.get(context).details.removePage(context, choicePage);
      choicePage.destroy(context);
      choicePage = null;
    }
    super.destroy(context);
  }

  /**
   * @param editor
   * @return true if consumed, false if text should proceed to value
   */
  private boolean textChanged(final Editor editor, String text) {
    Context context = editor.context;
    Atom gap = gapAtom();

    /// Clean up before new state
    if (choicePage != null) {
      editor.details.removePage(context, choicePage);
      choicePage.destroy(context);
    }

    /// Parse new text, rank choices
    // If the whole text matches, try to auto complete
    // Display info on matches and not-yet-mismatches
    TSList<Event> glyphs = context.env.splitGlyphEvents(text);
    final Pair<Step<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>, Position>
        longest = new ParseBuilder<>(GAP_ROOT_KEY).grammar(grammar).longestMatchFromStart(glyphs);
    TSList<GapChoice> choices = new TSList<>();
    TSSet<AtomType> seen = new TSSet<>();
    for (boolean completed : new boolean[] {true, false}) {
      for (ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>> result :
          longest.first.completed) {
        if (completed != !result.second.escaped) continue;
        PreGapChoice choice = result.first;
        seen.add(result.first.type);
        choices.add(
            new GapChoice(
                gap,
                choice.type,
                choice.consumePreceding,
                choice.supplyFillAtoms,
                glyphs,
                ((int) longest.second.at) + 1,
                result.second.value,
                null,
                choice.keySpecs,
                choice.following));
      }
    }

    /// Choose auto-choosable choices
    if ((int) longest.second.at + 1 == glyphs.size()) {
      for (final GapChoice choice : choices) {
        if (choice.type.autoChooseUnambiguous && choices.size() == 1) {
          choice.choose(editor);
          return true;
        }
      }
    } else if ((int) longest.second.at > 0) {
      // While typing, whole text will generally continue to match
      // If there's a typo, nothing will match (at == 0)
      // If has typed a whole candidate and continue to suffix, match will be < full, > 0 (ex:
      // text.length() - 1)
      // -> When the text stops matching (new element started?) go ahead and choose the
      // closest-to-full choice
      for (final GapChoice choice : choices) {
        choice.choose(editor);
        return true;
      }
    }

    /// Update visual
    if (!choices.isEmpty()) {
      choicePage = new TwoColumnChoicePage(editor, (TSList<TwoColumnChoice>) (TSList) choices);
      editor.details.addPage(context, choicePage);
    } else {
      if (choicePage != null) {
        editor.details.removePage(context, choicePage);
        choicePage.destroy(context);
        choicePage = null;
      }
    }
    return false;
  }

  @FunctionalInterface
  public interface PrepareAtomField {
    /**
     * Prepares a field and populates with consumed atoms (creates history). Atoms must have been
     * already removed from their parents.
     *
     * @return
     */
    public Field process(Editor editor, History.Recorder recorder);
  }
}
