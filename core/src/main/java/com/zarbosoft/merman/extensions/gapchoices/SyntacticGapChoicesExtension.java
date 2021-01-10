package com.zarbosoft.merman.extensions.gapchoices;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.gap.GapCompletionEngine;
import com.zarbosoft.merman.editor.gap.GapKey;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.extensions.Extension;
import com.zarbosoft.merman.extensions.ExtensionContext;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.front.*;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.*;

public class SyntacticGapChoicesExtension extends Extension {
  private static final Supplier<Placement> createPlaceArray = () -> new PlaceArray();
  private static final Supplier<Placement> createPlaceAtom = () -> new PlaceAtom();

  @Override
  public State create(ExtensionContext context) {
    return new State(context);
  }

  public interface Placement {
    public void add(Atom value);
  }

  private static class PlaceArray implements Placement {
    public final List<Atom> data = new ArrayList<>();

    @Override
    public void add(Atom value) {
      data.add(value);
    }
  }

  private static class PlaceAtom implements Placement {
    public Atom data;

    @Override
    public void add(Atom value) {
      if (this.data != null) throw new Assertion();
      this.data = value;
    }
  }

  private static class AtomEvent implements Event {
    public final Atom atom;

    AtomEvent(Atom atom) {
      this.atom = atom;
    }
  }

  private static class TypeMatch extends Terminal {
    public final String type;

    TypeMatch(String type) {
      this.type = type;
    }

    @Override
    protected boolean matches(Event event, Store store) {
      return ((ExtensionContext) store.env.get("context"))
          .syntax()
          .splayedTypes
          .get(type)
          .contains(((AtomEvent) event).atom.type);
    }
  }

  private static class State extends Extension.State implements GapCompletionEngine {

    public State(ExtensionContext context) {
      context.gapCompletionEngine = this;
    }

    @Override
    public void destroy(ExtensionContext context) {
      context.gapCompletionEngine = null;
    }

    @Override
    public State createGapCompletionState(ExtensionContext context, String baseType) {
      GapCompletionState state = new GapCompletionState();
      Union grammar = new Union();
      Deque<Iterator<FreeAtomType>> stack = new ArrayDeque<>();
      stack.add(context.syntax().splayedTypes.get(baseType).iterator());
      while (!stack.isEmpty()) {
        Iterator<FreeAtomType> top = stack.peekLast();
        FreeAtomType type = top.next();
        if (!top.hasNext()) {
          stack.removeLast();
        }
        FrontSpec front = type.front.get(0);
        if (front instanceof FrontSymbol) {
          createGapChoice(state, grammar, GapKey.gapKeys(type).get(0), type);
        } else if (front instanceof FrontArraySpecBase) {
          if (((FrontArraySpecBase) front).prefix.isEmpty()) {
            stack.add(
                context
                    .syntax()
                    .splayedTypes
                    .get(((FrontArraySpecBase) front).dataType.elementAtomType())
                    .iterator());
          } else {
            createGapChoice(state, grammar, GapKey.gapKeys(type).get(0), type);
          }
        } else if (front instanceof FrontAtomSpec) {
          stack.add(
              context.syntax().splayedTypes.get(((FrontAtomSpec) front).dataType.type).iterator());
        } else if (front instanceof FrontPrimitiveSpec) {
          createGapChoice(state, grammar, GapKey.gapKeys(type).get(0), type);
        }
      }
      state.grammar = new Grammar().add(Grammar.DEFAULT_ROOT_KEY, grammar);
      return state;
    }

    /**
     * Finds the next selection point, null if nowhere to select (needs to be wrapped in suffix gap)
     *
     * @return First is root of tree to place, second is value where cursor goes after placement
     */
    private Value findSelectNext(
        GapKey.ParseResult parsed, GapKey key, Atom atom, FreeAtomType type) {
      Value selectNext = null;
      FrontSpec selectNextFront = null;
      if (parsed.nextPrimitive == null) {
        if (key.indexAfter == -1) {
          // nop
        } else {
          selectNextFront = type.front.get(key.indexAfter);
        }
      } else {
        selectNextFront = parsed.nextPrimitive;
      }
      if (selectNext == null && selectNextFront != null) {
        if (selectNextFront instanceof FrontAtomSpec) {
          selectNext = atom.fields.getOpt(selectNextFront.field());
        } else if (selectNextFront instanceof FrontPrimitiveSpec
            || selectNextFront instanceof FrontArraySpecBase) {
          selectNext = atom.fields.getOpt(selectNextFront.field());
        } else throw new DeadCode();
      }
      return selectNext;
    }

    private void createGapChoice(
        GapCompletionState state, Union grammar, GapKey key, FreeAtomType type) {
      grammar.add(
          key.matchGrammar(
              new SyntacticGapChoice(type, key) {
                @Override
                public void choose(Context context) {
                  /// Build atom
                  final GapKey.ParseResult parsed = key.parse(context, type, state.currentText);

                  /// Find the selection/remainder entry point
                  Atom generatedRoot = parsed.atom;
                  Value selectNext = findSelectNext(parsed, key, generatedRoot, type);

                  /// Place the atom
                  if (selectNext == null) {
                    generatedRoot = context.syntax.suffixGap.create(true, generatedRoot);
                    selectNext = (ValuePrimitive) generatedRoot.fields.getOpt("gap");
                  } else {
                    generatedRoot = parsed.atom;
                  }
                  gap.parent.replace(context, generatedRoot);

                  /// Select and dump remainder
                  if (selectNext instanceof ValueAtom
                      && ((ValueAtom) selectNext).data.visual.selectDown(context)) {
                  } else selectNext.selectDown(context);
                  if (!parsed.remainder.isEmpty())
                    context.cursor.receiveText(context, parsed.remainder);
                }
              }));
    }

    private Grammar findPrecedingPlacementsGrammar(GapKey gapKey, FreeAtomType type) {
      TSMap<FrontSpec, Supplier<Placement>> placementsTemplate = new TSMap<>();
      Sequence precedingConsumptionGrammar = new Sequence().add(StackStore.prepVarStack);
      for (int frontIndex = gapKey.indexBefore; frontIndex >= 0; --frontIndex) {
        FrontSpec front = type.front.get(frontIndex);
        if (front instanceof FrontArraySpecBase) {
          placementsTemplate.put(front, createPlaceArray);
          precedingConsumptionGrammar.add(
              new Repeat(
                  new Operator<StackStore>(
                      new TypeMatch(((FrontArraySpecBase) front).dataType.elementAtomType())) {
                    @Override
                    protected StackStore process(StackStore store) {
                      Atom atom = ((AtomEvent) store.top()).atom;
                      return store.stackVarDoubleElement(front, atom);
                    }
                  }));

        } else if (front instanceof FrontAtomSpec) {
          placementsTemplate.put(front, createPlaceAtom);
          precedingConsumptionGrammar.add(
              new Operator<StackStore>(new TypeMatch(((FrontAtomSpec) front).dataType.type)) {
                @Override
                protected StackStore process(StackStore store) {
                  return store.stackVarDoubleElement(front, ((AtomEvent) store.top()).atom);
                }
              });
        } else throw new Assertion();
      }
      precedingConsumptionGrammar.add(
          new Operator<StackStore>() {
            @Override
            protected StackStore process(StackStore store) {
              TSMap<FrontSpec, Placement> placements = new TSMap<>();
              for (Map.Entry<FrontSpec, Supplier<Placement>> e : placementsTemplate.entries()) {
                placements.put(e.getKey(), e.getValue().get());
              }
              return store
                  .<FrontSpec, Atom>popVarDouble(
                      (i, atom) -> {
                        placements.get(i).add(atom);
                      })
                  .pushStack(placements);
            }
          });

      return new Grammar().add("root", precedingConsumptionGrammar);
    }

    @Override
    public State createSuffixGapCompletionState(
        ExtensionContext context, List<Atom> preceding, String baseType) {
      GapCompletionState state = new GapCompletionState();
      Union gapTextGrammar = new Union();
      Deque<Iterator<FreeAtomType>> stack = new ArrayDeque<>();
      stack.add(context.syntax().splayedTypes.get(baseType).iterator());
      while (!stack.isEmpty()) {
        Iterator<FreeAtomType> top = stack.peekLast();
        FreeAtomType type = top.next();
        if (!top.hasNext()) {
          stack.removeLast();
        }

        List<GapKey> keys = GapKey.gapKeys(type);
        GapKey gapKey = keys.get(0);

        /// Check if all front slots before gap match preceding atoms, if so this is a candidate
        Grammar precedingPlacementsGrammar = findPrecedingPlacementsGrammar(gapKey, type);
        try {
          List<Atom> reversePreceding = new ArrayList<>(preceding);
          Collections.reverse(reversePreceding);
          Pair<Parse, Position> longestMatch =
              new ParseBuilder<TSMap<Integer, Placement>>()
                  .grammar(precedingPlacementsGrammar)
                  .store(new StackStore())
                  .uncertainty(100)
                  .root("root")
                  .longestMatchFromStart(
                      reversePreceding.stream()
                          .map(a -> new AtomEvent(a))
                          .collect(Collectors.toList()));
          createSuffixGapChoice(
              state,
              gapTextGrammar,
              gapKey,
              type,
              ((TSMap<FrontSpec, Placement>) longestMatch.first.results.get(0)),
              (Integer) longestMatch.second.at + 1);
        } catch (InvalidStream | GrammarTooUncertain ignored) {
        }

        /// Descend into slots preceding key
        for (int i = 0; i <= gapKey.indexBefore; ++i) {
          FrontSpec spec = type.front.get(i);
          if (spec instanceof FrontArraySpecBase) {
            stack.addLast(
                context
                    .syntax()
                    .splayedTypes
                    .get(((FrontArraySpecBase) spec).dataType.elementAtomType())
                    .iterator());
          } else if (spec instanceof FrontAtomSpec) {
            stack.addLast(
                context.syntax().splayedTypes.get(((FrontAtomSpec) spec).dataType.type).iterator());
          } else throw new Assertion();
        }
      }
      state.grammar = new Grammar().add(Grammar.DEFAULT_ROOT_KEY, gapTextGrammar);
      return state;
    }

    private void createSuffixGapChoice(
        GapCompletionState state,
        Union grammar,
        GapKey key,
        FreeAtomType type,
        TSMap<FrontSpec, Placement> precedingPlacements,
        int precedingConsumed) {
      grammar.add(
          key.matchGrammar(
              new SyntacticGapChoice(type, key) {
                @Override
                public void choose(Context context) {
                  ValueArray precedingValue = (ValueArray) gap.fields.getOpt("value");

                  Value.Parent placeGeneratedAt = gap.parent;

                  /// Parse text into atom as able
                  final GapKey.ParseResult parsed = key.parse(context, type, state.currentText);
                  final Atom atom = parsed.atom;
                  final String remainder = parsed.remainder;

                  /// Find the selection/remainder entry point
                  Atom generatedRoot = parsed.atom;
                  Value selectNext = findSelectNext(parsed, key, generatedRoot, type);

                  /// Place everything starting from the bottom
                  List<Atom> unconsumed =
                      new ArrayList<>(sublist(precedingValue.data, 0, -precedingConsumed));
                  // Remove preceding atoms
                  context.history.apply(
                      context,
                      new ChangeArray(
                          precedingValue, 0, precedingValue.data.size(), ImmutableList.of()));
                  // Place root, wrap in gap if necessary (and place unconsumed preceding atoms)
                  if (selectNext == null || !unconsumed.isEmpty()) {
                    unconsumed.add(generatedRoot);
                    generatedRoot = context.syntax.suffixGap.create(true, unconsumed);
                    if (selectNext == null)
                      selectNext = (ValuePrimitive) generatedRoot.fields.getOpt("gap");
                  } else {
                    generatedRoot = parsed.atom;
                  }
                  placeGeneratedAt.replace(context, generatedRoot);
                  // Place consumed preceding atoms
                  for (Map.Entry<FrontSpec, Placement> entry : precedingPlacements.entries()) {
                    Value dest = atom.fields.get(entry.getKey().field());
                    if (dest instanceof ValueAtom)
                      context.history.apply(
                          context,
                          new ChangeNodeSet((ValueAtom) dest, ((PlaceAtom) entry.getValue()).data));
                    else if (dest instanceof ValueArray)
                      context.history.apply(
                          context,
                          new ChangeArray(
                              (ValueArray) dest, 0, 0, ((PlaceArray) entry.getValue()).data));
                    else throw new DeadCode();
                  }

                  /// Select and dump remainder of the text
                  if (selectNext instanceof ValueAtom
                      && ((ValueAtom) selectNext).data.visual.selectDown(context)) {
                  } else selectNext.selectDown(context);
                  if (!remainder.isEmpty()) context.cursor.receiveText(context, remainder);
                }
              }));
    }
  }
}
