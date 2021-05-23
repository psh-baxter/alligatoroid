package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.BaseEditCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeAtom;
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
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class EditGapCursorFieldPrimitive extends BaseEditCursorFieldPrimitive {
  public static Reference.Key<ROList<PrepareAtomField>> PRECEDING_ROOT_KEY = new Reference.Key<>();
  public static Reference.Key<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>
      GAP_ROOT_KEY = new Reference.Key<>();
  public final Grammar grammar;
  public String currentText;
  public TwoColumnChoicePage choicePage;

  public EditGapCursorFieldPrimitive(
      Editor editor,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(editor.context, visualPrimitive, leadFirst, beginOffset, endOffset);
    grammar = createChoiceGrammar(editor.context);
    textChanged(editor, null, visualPrimitive.value.data.toString());
  }

  @Override
  public void editHandleTyping(Editor editor, History.Recorder recorder, String text) {
    FieldPrimitive value = visualPrimitive.value;
    String preview = value.get();
    preview =
        preview.substring(0, range.beginOffset)
            + text
            + preview.substring(range.endOffset, preview.length());
    if (textChanged(editor, recorder, preview)) {
      return;
    }
    super.editHandleTyping(editor, recorder, text);
  }

  private Grammar createChoiceGrammar(Context context) {
    // Walk all directly and indirectly reachable types, creating a choice for each
    Atom gap = gapAtom();
    AtomType gapType = gap.type;
    String baseType = gap.fieldParentRef.valueType();
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
                            recorder.apply(editor.context, new ChangeAtom(field, value));
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

  @Override
  public void editCut(Editor editor) {
    super.editCut(editor);
    textChangedNoAutocomplete(editor, visualPrimitive.value.get());
  }

  @Override
  public void editDeleteNext(Editor editor) {
    super.editDeleteNext(editor);
    textChangedNoAutocomplete(editor, visualPrimitive.value.get());
  }

  @Override
  public void editDeletePrevious(Editor editor) {
    super.editDeletePrevious(editor);
    textChangedNoAutocomplete(editor, visualPrimitive.value.get());
  }

  public TextChangedResult textChangedNoAutocomplete(Editor editor, String text) {
    Context context = editor.context;
    Atom gap = gapAtom();

    /// Clean up before new state
    if (choicePage != null) {
      editor.details.removePage(context, choicePage);
      choicePage.destroy(context);
    }

    TextChangedResult out = new TextChangedResult();

    /// Parse new text, rank choices
    // If the whole text matches, try to auto complete
    // Display info on matches and not-yet-mismatches
    out.glyphs = context.env.splitGlyphEvents(text);
    out.longest =
        new ParseBuilder<>(GAP_ROOT_KEY).grammar(grammar).longestMatchFromStart(out.glyphs);
    out.choices = new TSList<>();
    TSSet<AtomType> seen = new TSSet<>();
    for (ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>> result :
        out.longest.first.completed) {
      PreGapChoice choice = result.first;
      seen.add(result.first.type);
      out.choices.add(
          new GapChoice(
              gap,
              choice.type,
              choice.consumePreceding,
              choice.supplyFillAtoms,
              out.glyphs,
              ((int) out.longest.second.at) + 1,
              result.second.value,
              null,
              choice.keySpecs,
              choice.following));
    }
    for (Step.Branch leaf : out.longest.first.branches) {
      PreGapChoice choice = (PreGapChoice) leaf.color();
      if (!seen.addNew(choice.type)) continue;
      out.choices.add(
          new GapChoice(
              gap,
              choice.type,
              choice.consumePreceding,
              choice.supplyFillAtoms,
              out.glyphs,
              ((int) out.longest.second.at) + 1,
              null,
              leaf,
              choice.keySpecs,
              choice.following));
    }

    /// Update visual
    if (!out.choices.isEmpty()) {
      choicePage = new TwoColumnChoicePage(editor, (TSList<TwoColumnChoice>) (TSList) out.choices);
      editor.details.setPage(editor, choicePage);
    } else {
      if (choicePage != null) {
        editor.details.removePage(context, choicePage);
        choicePage.destroy(context);
        choicePage = null;
      }
    }

    return out;
  }

  /**
   * @param editor
   * @return true if consumed, false if text should proceed to value
   */
  public boolean textChanged(final Editor editor, History.Recorder recorder, String text) {
    TextChangedResult res = textChangedNoAutocomplete(editor, text);

    /// Choose auto-choosable choices
    if (res.glyphs.some()) {
      if ((int) res.longest.second.at + 1 == res.glyphs.size()) {
        for (final GapChoice choice : res.choices) {
          if (choice.type.autoChooseUnambiguous && res.choices.size() == 1) {
            choice.choose(editor, recorder);
            return true;
          }
        }
      } else if ((int) res.longest.second.at >= 0) {
        // While typing, whole text will generally continue to match
        // If there's a typo, nothing will match (at == 0)
        // If has typed a whole candidate and continue to suffix, match will be < full, > 0 (ex:
        // text.length() - 1)
        // -> When the text stops matching (new element started?) go ahead and choose the
        // closest-to-full choice
        for (final GapChoice choice : res.choices) {
          choice.choose(editor, recorder);
          return true;
        }
      }
    }

    return false;
  }

  public void editExit(Editor editor) {
    Atom.Parent atomParentRef = visualPrimitive.value.atomParentRef;
    if (atomParentRef == null) return;
    Atom gap = atomParentRef.atom();
    atomParentRef.selectParent(editor.context);
    Field gapInField = gap.fieldParentRef.field;
    if (gap.type == editor.context.syntax.gap)
      do {
        if (!(gapInField instanceof FieldArray)) break;
        FieldArray value = (FieldArray) gapInField;
        TSList<Atom> data = value.data;
        if (data.size() > 1) break;
        Atom atom = data.get(0);
        if (atom.type != editor.context.syntax.gap) break;
        FieldPrimitive field = (FieldPrimitive) atom.fields.get(GapAtomType.PRIMITIVE_KEY);
        if (!field.get().isEmpty()) break;
        editor.history.record(
            editor.context,
            null,
            recorder -> {
              recorder.apply(editor.context, new ChangeArray(value, 0, 1, ROList.empty));
            });
      } while (false);
    else
      do {
        /// Remove empty syntax gaps and place lifted preceding back into parent
        FieldPrimitive prim = visualPrimitive.value;
        if (!prim.get().isEmpty()) break;
        FieldArray array = (FieldArray) gap.fields.get(SuffixGapAtomType.PRECEDING_KEY);
        ROOrderedSetRef<AtomType> canPlace;
        if (array.data.size() == 0) {
          canPlace = ROOrderedSetRef.empty;
        } else if (array.data.size() == 1 && gapInField instanceof FieldAtom) {
          canPlace = editor.context.syntax.splayedTypes.get(((FieldAtom) gapInField).back().type);
        } else if (gapInField instanceof FieldArray) {
          canPlace =
              editor.context.syntax.splayedTypes.get(
                  ((FieldArray) gapInField).back().elementAtomType());
        } else break;
        boolean canPlaceAll = true;
        for (Atom atom : array.data) {
          if (!canPlace.contains(atom.type)) {
            canPlaceAll = false;
            break;
          }
        }
        if (!canPlaceAll) break;
        TSList<Atom> transplant = array.data.mut();
        editor.history.record(
            editor.context,
            null,
            recorder -> {
              recorder.apply(
                  editor.context, new ChangeArray(array, 0, array.data.size(), ROList.empty));
              if (gapInField instanceof FieldAtom) {
                Atom transplant0;
                if (transplant.some()) transplant0 = transplant.get(0);
                else
                  transplant0 =
                      Editor.createEmptyAtom(editor.context.syntax, editor.context.syntax.gap);
                recorder.apply(editor.context, new ChangeAtom((FieldAtom) gapInField, transplant0));
              } else if (gapInField instanceof FieldArray) {
                recorder.apply(
                    editor.context,
                    new ChangeArray(
                        (FieldArray) gapInField,
                        ((FieldArray.Parent) gap.fieldParentRef).index,
                        1,
                        transplant));
              } else throw new Assertion();
            });
      } while (false);
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

  public static class TextChangedResult {
    public TSList<Event> glyphs;
    public TSList<GapChoice> choices;
    public Pair<Step<ROPair<PreGapChoice, EscapableResult<ROList<FieldPrimitive>>>>, Position>
        longest;
  }
}
