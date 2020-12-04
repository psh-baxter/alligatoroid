package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.merman.document.Atom;
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
import com.zarbosoft.merman.syntax.front.FrontArrayAsAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontDataAtom;
import com.zarbosoft.merman.syntax.front.FrontGapBase;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.bytes.stores.StackClipStore;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.iterable;

public class PrefixGapAtomType extends AtomType {
  private final List<BackSpec> back;
  private final BackPrimitiveSpec dataGap;
  private final BackArraySpec dataValue;
  public List<FrontSymbol> frontPrefix = new ArrayList<>();
  public List<FrontSymbol> frontInfix = new ArrayList<>();
  public List<FrontSymbol> frontSuffix = new ArrayList<>();
  private List<FrontSpec> front;

  public PrefixGapAtomType() {
    dataGap = new BackPrimitiveSpec();
    dataGap.id = "gap";
    dataValue = new BackArraySpec();
    dataValue.id = "value";
    dataValue.element = new BackAtomSpec();
    final BackFixedRecordSpec record = new BackFixedRecordSpec();
    record.pairs.put("gap", dataGap);
    record.pairs.put("value", dataValue);
    final BackFixedTypeSpec type = new BackFixedTypeSpec();
    type.type = "__prefix_gap";
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
      final FrontGapBase gap =
          new FrontGapBase() {
            @Override
            protected List<? extends Choice> process(
                final Context context,
                final Atom self,
                final String string,
                final Common.UserData store) {
              final Atom value = ((ValueArray) self.fields.getOpt("value")).data.get(0);
              class PrefixChoice extends Choice {
                private final FreeAtomType type;
                private final GapKey key;

                PrefixChoice(final FreeAtomType type, final GapKey key) {
                  this.type = type;
                  this.key = key;
                }

                public int ambiguity() {
                  return type.autoChooseAmbiguity;
                }

                public void choose(final Context context, final String string) {
                  // Build atom
                  final GapKey.ParseResult parsed = key.parse(context, type, string);
                  final Atom atom = parsed.atom;

                  // Place the atom
                  self.parent.replace(context, atom);

                  // Wrap the value in a prefix gap and place
                  final Atom inner =
                      parsed.nextInput == null ? context.syntax.prefixGap.create(value) : value;
                  type.front()
                      .get(key.indexAfter)
                      .dispatch(
                          new NodeOnlyDispatchHandler() {
                            @Override
                            public void handle(final FrontArraySpecBase front) {
                              context.history.apply(
                                  context,
                                  new ChangeArray(
                                      (ValueArray) atom.fields.getOpt(front.field()),
                                      0,
                                      0,
                                      ImmutableList.of(inner)));
                            }

                            @Override
                            public void handle(final FrontDataAtom front) {
                              context.history.apply(
                                  context,
                                  new ChangeNodeSet(
                                      (ValueAtom) atom.fields.getOpt(front.middle), inner));
                            }
                          });

                  // Select the next input after the key
                  if (parsed.nextInput != null)
                    atom.fields.getOpt(parsed.nextInput.field()).selectDown(context);
                  else inner.visual.selectDown(context);
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
                  return type == ((PrefixChoice) obj).type;
                }
              }

              // Get or build gap grammar
              final Grammar grammar =
                  store.get(
                      () -> {
                        final Union union = new Union();
                        for (final FreeAtomType type :
                            context.syntax.getLeafTypes(self.parent.childType())) {
                          for (final GapKey key : gapKeys(syntax, type, value.type)) {
                            if (key.indexAfter == -1) continue;
                            final PrefixChoice choice = new PrefixChoice(type, key);
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
              if (longest.second.absolute == string.length()) {
                final List<PrefixChoice> choices =
                    Stream.concat(
                            longest.first.results.stream().map(result -> (PrefixChoice) result),
                            longest.first.leaves.stream().map(leaf -> (PrefixChoice) leaf.color()))
                        .distinct()
                        .collect(Collectors.toList());
                for (final PrefixChoice choice : choices) {
                  if (choices.size() <= choice.ambiguity()) {
                    choice.choose(context, string);
                    return ImmutableList.of();
                  }
                }
                return choices;
              }
              return ImmutableList.of();
            }

            @Override
            protected void deselect(
                final Context context,
                final Atom self,
                final String string,
                final Common.UserData userData) {
              if (self.visual != null && string.isEmpty()) {
                self.parent.replace(context, ((ValueArray) self.fields.getOpt("value")).data.get(0));
              }
            }
          };
      final FrontArrayAsAtomSpec value = new FrontArrayAsAtomSpec();
      value.back = "value";
      front =
          ImmutableList.copyOf(
              Iterables.concat(
                  frontPrefix,
                  ImmutableList.of(gap),
                  frontInfix,
                  ImmutableList.of(value),
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
    return "__prefix_gap";
  }

  @Override
  public String name() {
    return "Gap (prefix)";
  }

  public Atom create(final Atom value) {
    return new Atom(
        this,
        new TSMap<>(
            ImmutableMap.of(
                "value",
                new ValueArray(dataValue, ImmutableList.of(value)),
                "gap",
                new ValuePrimitive(dataGap, ""))));
  }

  public Atom create() {
    return new Atom(
        this,
        new TSMap<>(
            ImmutableMap.of(
                "value",
                new ValueArray(dataValue, ImmutableList.of()),
                "gap",
                new ValuePrimitive(dataGap, ""))));
  }
}
