package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontArrayAsAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontGapBase;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.editor.gap.GapKey;
import com.zarbosoft.merman.editor.gap.TwoColumnChoice;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.bytes.stores.StackClipStore;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuffixGapAtomType extends AtomType {
  private final BackArraySpec dataValue;
  private final BaseBackPrimitiveSpec dataGap;
  private final List<BackSpec> back;
  public List<FrontSymbol> frontPrefix = new ArrayList<>();
  public List<FrontSymbol> frontInfix = new ArrayList<>();
  public List<FrontSymbol> frontSuffix = new ArrayList<>();
  private List<FrontSpec> front;

  public SuffixGapAtomType() {
    dataValue = new BackArraySpec();
    dataValue.id = "value";
    dataValue.element = new BackAtomSpec();
    dataGap = new BackPrimitiveSpec();
    dataGap.id = "gap";
    final BackFixedRecordSpec record = new BackFixedRecordSpec();
    record.pairs.put("value", dataValue);
    record.pairs.put("gap", dataGap);
    final BackFixedTypeSpec type = new BackFixedTypeSpec();
    type.type = "__suffix_gap";
    type.value = record;
    back = ImmutableList.of(type);
  }

  @Override
  public Map<String, AlignmentDefinition> alignments() {
    return ImmutableMap.of();
  }

  @Override
  public int precedence() {
    return 1_000_000;
  }

  @Override
  public boolean associateForward() {
    return false;
  }

  @Override
  public int depthScore() {
    return 0;
  }

  @Override
  public void finish(
    List<Object> errors, final Syntax syntax
  ) {
    {
      final FrontArrayAsAtomSpec value = new FrontArrayAsAtomSpec();
      value.back = "value";
      final FrontGapBase gap =
          new FrontGapBase() {
            @Override
            public List<? extends TwoColumnChoice> process(
              final Context context, final Atom self, final String string, final Common.UserData store
            ) {
              final SuffixGapAtom suffixSelf = (SuffixGapAtom) self;
              class SuffixChoice extends TwoColumnChoice {
                private final FreeAtomType type;
                private final GapKey key;

                public SuffixChoice(final FreeAtomType type, final GapKey key) {
                  this.type = type;
                  this.key = key;
                }

                public int ambiguity() {
                  return type.autoChooseAmbiguity;
                }

                public void choose(final Context context, final String string) {
                  Atom root;
                  Value.Parent rootPlacement;
                  Atom child;
                  final Value childPlacement;
                  Atom child2 = null;
                  Value.Parent child2Placement = null;

                  // Parse text into atom as possible
                  final GapKey.ParseResult parsed = key.parse(context, type, string);
                  final Atom atom = parsed.atom;
                  final String remainder = parsed.remainder;
                  root = atom;
                  child = ((ValueArray) suffixSelf.fields.getOpt("value")).data.get(0);
                  childPlacement = atom.fields.getOpt(atom.type.front().get(key.indexBefore).field());

                  // Find the new atom placement point
                  rootPlacement = suffixSelf.parent;
                  if (suffixSelf.raise) {
                    final Pair<Value.Parent, Atom> found =
                        findReplacementPoint(
                            context, rootPlacement, (FreeAtomType) parsed.atom.type);
                    if (found.first != rootPlacement) {
                      // Raising new atom up; the value will be placed at the original parent
                      child2 = child;
                      child = found.second;
                      child2Placement = suffixSelf.parent;
                      rootPlacement = found.first;
                    }
                  }

                  // Find the selection/remainder entry point
                  Value selectNext = null;
                  FrontSpec nextWhatever = null;
                  if (parsed.nextPrimitive == null) {
                    if (key.indexAfter == -1) {
                      // No such place exists - wrap the placement atom in a suffix gap
                      root = context.syntax.suffixGap.create(true, atom);
                      selectNext = (ValuePrimitive) root.fields.getOpt("gap");
                    } else {
                      nextWhatever = type.front.get(key.indexAfter);
                    }
                  } else nextWhatever = parsed.nextPrimitive;
                  if (selectNext == null) {
                    if (nextWhatever instanceof FrontAtomSpec) {
                      selectNext = atom.fields.getOpt(nextWhatever.field());
                    } else if (nextWhatever instanceof FrontPrimitiveSpec
                        || nextWhatever instanceof FrontArraySpecBase) {
                      selectNext = atom.fields.getOpt(nextWhatever.field());
                    } else throw new DeadCode();
                  }

                  // Place everything starting from the bottom
                  rootPlacement.replace(context, root);
                  if (childPlacement instanceof ValueAtom)
                    context.history.apply(
                        context, new ChangeNodeSet((ValueAtom) childPlacement, child));
                  else if (childPlacement instanceof ValueArray)
                    context.history.apply(
                        context,
                        new ChangeArray(
                            (ValueArray) childPlacement, 0, 0, ImmutableList.of(child)));
                  else throw new DeadCode();
                  if (child2Placement != null) child2Placement.replace(context, child2);

                  // Select and dump remainder
                  if (selectNext instanceof ValueAtom
                      && ((ValueAtom) selectNext).data.visual.selectDown(context)) {
                  } else selectNext.selectDown(context);
                  if (!remainder.isEmpty()) context.cursor.receiveText(context, remainder);
                }

                @Override
                public String name() {
                  return type.name();
                }

                @Override
                public Iterable<? extends FrontSpec> parts() {
                  return key.keyParts;
                }

                @Override
                public boolean equals(final Object obj) {
                  return type == ((SuffixChoice) obj).type;
                }
              }

              // Get or build gap grammar
              final AtomType childType =
                  ((ValueArray) suffixSelf.fields.getOpt("value")).data.get(0).type;
              final Grammar grammar =
                  store.get(
                      () -> {
                        final Union union = new Union();
                        for (final FreeAtomType type : context.syntax.types.values()) {
                          final Pair<Value.Parent, Atom> replacementPoint =
                              findReplacementPoint(context, self.parent, type);
                          if (replacementPoint.first == null) continue;
                          for (final GapKey key : gapKeys(syntax, type, childType)) {
                            if (key.indexBefore == -1) continue;
                            final TwoColumnChoice choice = new SuffixChoice(type, key);
                            union.add(
                                new Color(
                                    choice,
                                    new Operator<StackClipStore>(key.matchGrammar(type)) {
                                      @Override
                                      protected StackClipStore process(StackClipStore store) {
                                        return store.pushStack(choice);
                                      }
                                    }));
                          }
                        }
                        final Grammar out = new Grammar();
                        out.add("root", union);
                        return out;
                      });

              // If the whole text matches, try to auto complete
              // Display info on matches and not-yet-mismatches
              final Pair<Parse, Position> longest =
                  new ParseBuilder<>()
                      .grammar(grammar)
                      .longestMatchFromStart(
                          new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
              final List<SuffixChoice> choices =
                  Stream.concat(
                          longest.first.results.stream().map(result -> (SuffixChoice) result),
                          longest.first.leaves.stream().map(leaf -> (SuffixChoice) leaf.color()))
                      .distinct()
                      .collect(Collectors.toList());
              if (longest.second.absolute == string.length()) {
                for (final SuffixChoice choice : choices) {
                  if (choices.size() <= choice.ambiguity()) {
                    choice.choose(context, string);
                    return ImmutableList.of();
                  }
                }
                return choices;
              } else if (longest.second.absolute >= 1) {
                for (final TwoColumnChoice choice : choices) {
                  choice.choose(context, string);
                  return ImmutableList.of();
                }
              }
              return ImmutableList.of();
            }

            private Pair<Value.Parent, Atom> findReplacementPoint(
                final Context context, final Value.Parent start, final FreeAtomType type) {
              Value.Parent parent = null;
              Atom child = null;
              Value.Parent test = start;
              // Atom testAtom = test.value().parent().atom();
              Atom testAtom = null;
              while (test != null) {
                boolean allowed = false;

                if (context
                    .syntax
                    .getLeafTypes(test.childType())
                    .stream().map(t -> t.id())
                    .collect(Collectors.toSet())
                    .contains(type.id())) {
                  parent = test;
                  child = testAtom;
                  allowed = true;
                }

                testAtom = test.value().parent.atom();
                if (testAtom.parent == null) break;

                if (!isPrecedent(type, test, allowed)) break;

                test = testAtom.parent;
              }
              return new Pair<>(parent, child);
            }

            @Override
            public void deselect(
              final Context context, final Atom self, final String string, final Common.UserData userData
            ) {
              if (self.visual != null && string.isEmpty()) {
                self.parent.replace(context, ((ValueArray) self.fields.getOpt("value")).data.get(0));
              }
            }
          };
      front =
          ImmutableList.copyOf(
              Iterables.concat(
                  frontPrefix,
                  ImmutableList.of(value),
                  frontInfix,
                  ImmutableList.of(gap),
                  frontSuffix));
    }
    super.finish(errors, syntax);
  }

  @Override
  public List<FrontSpec> front() {
    return front;
  }

  @Override
  public List<BackSpec> back() {
    return back;
  }

  @Override
  public String id() {
    return "__suffix_gap";
  }

  @Override
  public String name() {
    return "Gap (suffix)";
  }

  public Atom create(final boolean raise, final Atom value) {
    return new SuffixGapAtom(
        this,
        new TSMap<>(
            ImmutableMap.of(
                "value",
                new ValueArray(dataValue, ImmutableList.of(value)),
                "gap",
                new ValuePrimitive(dataGap, ""))),
        raise);
  }

  private static class SuffixGapAtom extends Atom {
    private final boolean raise;

    public SuffixGapAtom(
        final AtomType type, final TSMap<String, Value> data, final boolean raise) {
      super(type, data);
      this.raise = raise;
    }
  }
}
