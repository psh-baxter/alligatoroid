package com.zarbosoft.merman.extensions.gapchoices;

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
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;

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
          .getLeafTypes(type, new HashSet<>())
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
      Set<String> seen = new HashSet<>();
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
                    .syntax().splayedTypes.get(((FrontArraySpecBase) front).dataType.elementAtomType())
                    .iterator());
          } else {
            createGapChoice(state, grammar, GapKey.gapKeys(type).get(0), type);
          }
        } else if (front instanceof FrontAtomSpec) {
          stack.add(
              context
                  .syntax()
                  .splayedTypes.get(((FrontAtomSpec) front).dataType.type)
                  .iterator());
        } else if (front instanceof FrontPrimitiveSpec) {
          createGapChoice(state, grammar, GapKey.gapKeys(type).get(0), type);
        }
      }
      state.grammar = new Grammar().add(Grammar.DEFAULT_ROOT_KEY, grammar);
      return state;
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
                  final Atom atom = parsed.atom;
                  final String remainder = parsed.remainder;
                  final Value.Parent rootPlacement = gap.parent;

                  /// Find the selection/remainder entry point
                  final Atom root;
                  Value selectNext = null;
                  FrontSpec selectNextFront = null;
                  if (parsed.nextPrimitive == null) {
                    if (key.indexAfter == -1) {
                      // No such place exists - wrap the placement atom in a suffix gap
                      root = context.syntax.suffixGap.create(true, atom);
                      selectNext = (ValuePrimitive) root.fields.getOpt("gap");
                    } else {
                      root = atom;
                      selectNextFront = type.front.get(key.indexAfter);
                    }
                  } else {
                    root = atom;
                    selectNextFront = parsed.nextPrimitive;
                  }
                  if (selectNext == null) {
                    if (selectNextFront instanceof FrontAtomSpec) {
                      selectNext = atom.fields.getOpt(selectNextFront.field());
                    } else if (selectNextFront instanceof FrontPrimitiveSpec
                        || selectNextFront instanceof FrontArraySpecBase) {
                      selectNext = atom.fields.getOpt(selectNextFront.field());
                    } else throw new DeadCode();
                  }

                  /// Place the atom
                  rootPlacement.replace(context, root);

                  /// Select and dump remainder
                  if (selectNext instanceof ValueAtom
                      && ((ValueAtom) selectNext).data.visual.selectDown(context)) {
                  } else selectNext.selectDown(context);
                  if (!remainder.isEmpty()) context.cursor.receiveText(context, remainder);
                }
              }));
    }

    @Override
    public State createPrefixGapCompletionState(
        ExtensionContext context, List<Atom> succeeding, String baseType) {
      GapCompletionState state = new GapCompletionState();
      Union grammar = new Union();
      for (FreeAtomType type : context.syntax().splayedTypes.get(baseType)) {
        List<GapKey> keys = GapKey.gapKeys(type);

        GapKey firstGapKey = keys.get(0);
        if (firstGapKey.indexBefore != -1) {
          // Don't deal with sandwitched insertions, if this starts with a gap
          continue;
        }

        GapKey lastGapKey = last(keys);
        if (lastGapKey.indexAfter == -1) {
          // Not a prefix
          continue;
        }

        TSMap<Integer, Supplier<Placement>> placementsTemplate = new TSMap<>();
        Sequence isCandidateSeq = new Sequence().add(StackStore.prepVarStack);
        for (int frontIndex0 = lastGapKey.indexAfter;
            frontIndex0 < type.front.size();
            ++frontIndex0) {
          int frontIndex = frontIndex0;
          FrontSpec front = type.front.get(frontIndex);
          if (front instanceof FrontArraySpecBase) {
            placementsTemplate.put(frontIndex, createPlaceArray);
            isCandidateSeq.add(
                new Repeat(
                    new Operator<StackStore>(
                        new TypeMatch(((FrontArraySpecBase) front).dataType.elementAtomType())) {
                      @Override
                      protected StackStore process(StackStore store) {
                        return store.stackVarDoubleElement(
                            frontIndex, ((AtomEvent) store.top()).atom);
                      }
                    }));
          } else if (front instanceof FrontAtomSpec) {
            placementsTemplate.put(frontIndex, createPlaceAtom);
            isCandidateSeq.add(
                new Operator<StackStore>(new TypeMatch(((FrontAtomSpec) front).dataType.type)) {
                  @Override
                  protected StackStore process(StackStore store) {
                    return store.stackVarDoubleElement(frontIndex, ((AtomEvent) store.top()).atom);
                  }
                });
          } else throw new Assertion();
        }
        isCandidateSeq.add(
            new Operator<StackStore>() {
              @Override
              protected StackStore process(StackStore store) {
                TSMap<Integer, Placement> placements = new TSMap<>();
                for (Map.Entry<Integer, Supplier<Placement>> e : placementsTemplate.entries()) {
                  placements.put(e.getKey(), e.getValue().get());
                }
                return store
                    .<Integer, Atom>popVarDouble(
                        (i, atom) -> {
                          placements.get(i).add(atom);
                        })
                    .pushStack(placements);
              }
            });

        /// Check if all front slots after gap match succeeding atoms, if so this is a candidate
        TSMap<Integer, Placement> placements = null;
        try {
          placements =
              new ParseBuilder<TSMap<Integer, Placement>>()
                  .grammar(new Grammar().add("root", isCandidateSeq))
                  .store(new StackStore())
                  .uncertainty(100)
                  .root("root")
                  .parse(
                      succeeding.stream().map(a -> new AtomEvent(a)).collect(Collectors.toList()));
        } catch (InvalidStream | GrammarTooUncertain ignored) {
        }
        if (placements != null) {
          createPrefixGapChoice(state, grammar, firstGapKey, type, placements);
        }
      }
      state.grammar = new Grammar().add(Grammar.DEFAULT_ROOT_KEY, grammar);
      return state;
    }

    private void createPrefixGapChoice(
        GapCompletionState state,
        Union grammar,
        GapKey key,
        FreeAtomType type,
        TSMap<Integer, Placement> placements) {
      grammar.add(
          key.matchGrammar(
              new SyntacticGapChoice(type, key) {
                @Override
                public void choose(Context context) {
                  final GapKey.ParseResult parsed = key.parse(context, type, state.currentText);
                  final Atom atom = parsed.atom;

                  // Place the atom
                  int lastPlacement = -1;
                  for (Map.Entry<Integer, Placement> entry : placements.entries()) {
                    if (entry.getKey() > lastPlacement) lastPlacement = entry.getKey();
                  }
                  FrontSpec nextDataFront = findNextDataAtom(type, lastPlacement + 1);
                  if (nextDataFront == null) {
                    Atom newGap  = context.syntax.prefixGap.create(atom);
                    gap.parent.replace(context, newGap);
                    doPlacements(context, atom);
                    newGap.visual.selectDown(context);
                  } else {
                    gap.parent.replace(context, atom);
                    doPlacements(context, atom);
                    atom.fields.getOpt(nextDataFront.field()).selectDown(context);
                  }
                }

                private void doPlacements(Context context, Atom atom) {
                  for (Map.Entry<Integer, Placement> entry : placements.entries()) {
                    FrontSpec front = type.front().get(entry.getKey());
                    Placement placement = entry.getValue();
                    if (placement instanceof PlaceAtom) {
                      context.history.apply(
                              context,
                          new ChangeNodeSet(
                              (ValueAtom) atom.fields.getOpt(front.field()),
                              ((PlaceAtom) placement).data));
                    } else if (placement instanceof PlaceArray) {
                      context.history.apply(
                              context,
                          new ChangeArray(
                              (ValueArray) atom.fields.getOpt(front.field()),
                              0,
                              0,
                              ((PlaceArray) placement).data));
                    } else throw new Assertion();
                  }
                }
              }));
    }

    @Override
    public State createSuffixGapCompletionState(
        ExtensionContext context, List<Atom> preceding, String baseType) {
      GapCompletionState state = new GapCompletionState();
      Union grammar = new Union();
      Deque<Iterator<String>> stack = new ArrayDeque<>();
      Set<String> seen = new HashSet<>();
      stack.add(Arrays.asList(baseType).iterator());
      while (!stack.isEmpty()) {
        Iterator<String> top = stack.peekLast();
        String next = top.next();
        if (!top.hasNext()) {
          stack.removeLast();
        }
        if (seen.contains(next)) continue;
        seen.add(next);
        List<String> foundGroup = context.syntax().groups.getOpt(next);
        if (foundGroup != null) {
          stack.add(foundGroup.iterator());
        } else {
          FreeAtomType type = context.syntax().types.get(next);
          List<GapKey> keys = GapKey.gapKeys(type);

          GapKey gapKey = keys.get(0);

          /// Create grammars matching front slots both away from the gap key and towards the gap
          // key from 0
          TSMap<Integer, Supplier<Placement>> placementsTemplate = new TSMap<>();
          /**
           * Matches front slots backwards from key towards the front start to see if they fully
           * match; preceding items in reverse
           */
          List<Node> preIsCandidateSeq = new ArrayList<>();
          /**
           * Matches front slots forward to see how far preceding atoms go; to figure out which slot
           * in a potential atom this gap is filling Increasing sublists of preceding are applied
           * until all front elements have been matched at least once
           */
          Sequence reachableSlotSeq = new Sequence().add(StackStore.prepVarStack);
          for (int frontIndex0 = 0; frontIndex0 <= gapKey.indexBefore; ++frontIndex0) {
            int frontIndex = frontIndex0;
            FrontSpec front = type.front.get(frontIndex);
            if (front instanceof FrontArraySpecBase) {
              placementsTemplate.put(frontIndex, createPlaceArray);
              TypeMatch typeMatch =
                  new TypeMatch(((FrontArraySpecBase) front).dataType.elementAtomType());
              reachableSlotSeq.add(typeMatch);
              preIsCandidateSeq.add(
                  new Repeat(
                      new Operator<StackStore>(typeMatch) {
                        @Override
                        protected StackStore process(StackStore store) {
                          return store.stackVarDoubleElement(
                              frontIndex, ((AtomEvent) store.top()).atom);
                        }
                      }));
            } else if (front instanceof FrontAtomSpec) {
              placementsTemplate.put(frontIndex, createPlaceAtom);
              TypeMatch typeMatch = new TypeMatch(((FrontAtomSpec) front).dataType.type);
              reachableSlotSeq.add(typeMatch);
              preIsCandidateSeq.add(
                  new Operator<StackStore>(typeMatch) {
                    @Override
                    protected StackStore process(StackStore store) {
                      return store.stackVarDoubleElement(
                          frontIndex, ((AtomEvent) store.top()).atom);
                    }
                  });
            } else throw new Assertion();
          }
          Sequence isCandidateSeq = new Sequence().add(StackStore.prepVarStack);
          Collections.reverse(preIsCandidateSeq);
          for (Node node : preIsCandidateSeq) isCandidateSeq.add(node);
          isCandidateSeq.add(
              new Operator<StackStore>() {
                @Override
                protected StackStore process(StackStore store) {
                  TSMap<Integer, Placement> placements = new TSMap<>();
                  for (Map.Entry<Integer, Supplier<Placement>> e : placementsTemplate.entries()) {
                    placements.put(e.getKey(), e.getValue().get());
                  }
                  return store
                      .<Integer, Atom>popVarDouble(
                          (i, atom) -> {
                            placements.get(i).add(atom);
                          })
                      .pushStack(placements);
                }
              });

          /// Check if all front slots before gap match preceding atoms, if so this is a candidate
          TSMap<Integer, Placement> placements = null;
          try {
            List<Atom> reversePreceding = new ArrayList<>(preceding);
            Collections.reverse(reversePreceding);
            placements =
                new ParseBuilder<TSMap<Integer, Placement>>()
                    .grammar(new Grammar().add("root", reachableSlotSeq))
                    .store(new StackStore())
                    .uncertainty(100)
                    .root("root")
                    .parse(
                        reversePreceding.stream()
                            .map(a -> new AtomEvent(a))
                            .collect(Collectors.toList()));
          } catch (InvalidStream | GrammarTooUncertain ignored) {
          }
          if (placements != null) {
            createSuffixGapChoice(state, grammar, gapKey, type, placements);
          }

          /// Descend into slots preceding key
          // Only descend into the first slot and slot immediately after slots that could be filled
          // by a preceding atom
          // (so 1 after 0-match count slots)
          // Is this overkill? Could just descend all preceding slots, although it would provide
          // less guidance
          if (preceding.size() >= gapKey.indexBefore + 1) {
            boolean grammarTooUncertain = false;
            for (int i = 0; i <= gapKey.indexBefore; ++i) {
              if (!grammarTooUncertain) {
                try {
                  new ParseBuilder<TSMap<Integer, Placement>>()
                      .grammar(new Grammar().add("root", reachableSlotSeq))
                      .store(new StackStore())
                      .uncertainty(100)
                      .root("root")
                      .parse(
                          preceding.subList(0, i + 1).stream()
                              .map(a -> new AtomEvent(a))
                              .collect(Collectors.toList()));
                } catch (GrammarTooUncertain ignored) {
                  grammarTooUncertain = true;
                } catch (InvalidStream ignored) {
                  continue;
                }
              }
              FrontSpec spec = type.front.get(i);
              if (spec instanceof FrontArraySpecBase) {
                stack.addLast(
                    Arrays.asList(((FrontArraySpecBase) spec).dataType.elementAtomType())
                        .iterator());
              } else if (spec instanceof FrontAtomSpec) {
                stack.addLast(Arrays.asList(((FrontAtomSpec) spec).dataType.type).iterator());
              } else throw new Assertion();
            }
          }
        }
      }
      state.grammar = new Grammar().add(Grammar.DEFAULT_ROOT_KEY, grammar);
      return state;
    }

    private void createSuffixGapChoice(
        GapCompletionState state,
        Union grammar,
        GapKey key,
        FreeAtomType type,
        TSMap<Integer, Placement> placements) {
      grammar.add(key.matchGrammar(state.new SyntacticGapChoice(type, key) {}));
    }
  }
}
