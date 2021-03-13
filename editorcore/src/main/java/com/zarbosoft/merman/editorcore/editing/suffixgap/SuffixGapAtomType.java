package com.zarbosoft.merman.editorcore.editing.suffixgap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Field;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.document.values.FieldAtom;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.editing.BaseGapAtomType;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.editing.FrontGapBase;
import com.zarbosoft.merman.editorcore.editing.GapKey;
import com.zarbosoft.merman.editorcore.editing.TwoColumnChoice;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontArrayAsAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.bytes.stores.StackClipStore;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuffixGapAtomType extends BaseGapAtomType {
  public static final String DEFAULT_TAG = "__suffix_gap";
  public static final String DEFAULT_ID = "__suffix_gap";
  public static final String GAP_ARRAY_KEY = "value";
  private final BackArraySpec backArray;
  private final BaseBackPrimitiveSpec backPrimitive;
  private final List<BackSpec> back;
  protected List<FrontSpec> front;


  public SuffixGapAtomType(EditingExtension edit, String type, String id, List<FrontSymbol> frontArrayPrefix, List<FrontSymbol> frontArrayInfix, List<FrontSymbol> frontArraySuffix, List<FrontSymbol> frontSuffix) {
    super(edit,id);
    {
      backArray = new BackArraySpec();
      backArray.id = GAP_ARRAY_KEY;
      backArray.type = new BackAtomSpec();
      backPrimitive = new BackPrimitiveSpec();
      backPrimitive.id = GAP_PRIMITIVE_KEY;
      final BackFixedRecordSpec backRecord =
          new BackFixedRecordSpec(
              new BackFixedRecordSpec.Config(
                  new TSMap<>(s -> s.put("value", backArray).put("gap", backPrimitive)), ROSet.empty));
      final BackFixedTypeSpec backType = new BackFixedTypeSpec();
      backType.type = type;
      backType.value = backRecord;
      back = Arrays.asList(backType);
    }
    {
      final FrontArraySpec frontArray = new FrontArraySpec();
      frontArray.back = GAP_ARRAY_KEY;
      frontArray.prefix = frontArrayPrefix;
      frontArray.separator = frontArrayInfix;
      frontArray.suffix = frontArraySuffix;
      final FrontGapBase frontPrimitive =
              new FrontGapBase() {
                @Override
                public void deselect(final Context context, final Atom self, final String string) {
                  if (!string.isEmpty()) return;
                  if (self.valueParentRef == null) return;
                  final Field parentField = self.valueParentRef.value;
                  if (parentField instanceof FieldArray) {
                    edit.arrayParentDelete((FieldArray.ArrayParent) self.valueParentRef);
                  }
                }
              };
      front = new ArrayList<>();
      front.add(frontArray);
      front.add(frontPrimitive);
      front.addAll(frontSuffix);
    }
  }

  @Override
  public void finish(
          MultiError errors, final Syntax syntax
  ) {
    {
      final FrontArrayAsAtomSpec value = new FrontArrayAsAtomSpec();
      value.field = "value";
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
                  Field.Parent rootPlacement;
                  Atom child;
                  final Field childPlacement;
                  Atom child2 = null;
                  Field.Parent child2Placement = null;

                  // Parse text into atom as possible
                  final GapKey.ParseResult parsed = key.parse(context, type, string);
                  final Atom atom = parsed.atom;
                  final String remainder = parsed.remainder;
                  root = atom;
                  child = ((FieldArray) suffixSelf.fields.getOpt("value")).data.get(0);
                  childPlacement = atom.fields.getOpt(atom.type.front().get(key.indexBefore).field());

                  // Find the new atom placement point
                  rootPlacement = suffixSelf.valueParentRef;
                  if (suffixSelf.raise) {
                    final Pair<Field.Parent, Atom> found =
                        findReplacementPoint(
                            context, rootPlacement, (FreeAtomType) parsed.atom.type);
                    if (found.first != rootPlacement) {
                      // Raising new atom up; the value will be placed at the original parent
                      child2 = child;
                      child = found.second;
                      child2Placement = suffixSelf.valueParentRef;
                      rootPlacement = found.first;
                    }
                  }

                  // Find the selection/remainder entry point
                  Field selectNext = null;
                  FrontSpec nextWhatever = null;
                  if (parsed.nextPrimitive == null) {
                    if (key.indexAfter == -1) {
                      // No such place exists - wrap the placement atom in a suffix gap
                      root = context.syntax.suffixGap.create(true, atom);
                      selectNext = (FieldPrimitive) root.fields.getOpt("gap");
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
                  if (childPlacement instanceof FieldAtom)
                    context.history.apply(
                        context, new ChangeNodeSet((FieldAtom) childPlacement, child));
                  else if (childPlacement instanceof FieldArray)
                    context.history.apply(
                        context,
                        new ChangeArray(
                            (FieldArray) childPlacement, 0, 0, ImmutableList.of(child)));
                  else throw new DeadCode();
                  if (child2Placement != null) child2Placement.replace(context, child2);

                  // Select and dump remainder
                  if (selectNext instanceof FieldAtom
                      && ((FieldAtom) selectNext).data.visual.selectAnyChild(context)) {
                  } else selectNext.selectInto(context);
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
                  ((FieldArray) suffixSelf.fields.getOpt("value")).data.get(0).type;
              final Grammar grammar =
                  store.get(
                      () -> {
                        final Union union = new Union();
                        for (final FreeAtomType type : context.syntax.types.values()) {
                          final Pair<Field.Parent, Atom> replacementPoint =
                              findReplacementPoint(context, self.valueParentRef, type);
                          if (replacementPoint.first == null) continue;
                          for (final GapKey key : gapKeys(syntax, type, childType)) {
                            if (key.indexBefore == -1) continue;
                            final TwoColumnChoice choice = new SuffixChoice(type, key);
                            union.add(
                                new Color(
                                    choice,
                                    new Operator<StackClipStore>(key.matchGrammar(context, type)) {
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

            private Pair<Field.Parent, Atom> findReplacementPoint(
                    final Context context, final Field.Parent start, final FreeAtomType type) {
              Field.Parent parent = null;
              Atom child = null;
              Field.Parent test = start;
              // Atom testAtom = test.value().parent().atom();
              Atom testAtom = null;
              while (test != null) {
                boolean allowed = false;

                if (context
                    .syntax
                    .getLeafTypes(test.valueType())
                    .stream().map(t -> t.id())
                    .collect(Collectors.toSet())
                    .contains(type.id())) {
                  parent = test;
                  child = testAtom;
                  allowed = true;
                }

                  testAtom = test.value.atomParentRef.atom();
                if (testAtom.valueParentRef == null) break;

                if (!AtomType.isPrecedent(type, test, allowed)) break;

                test = testAtom.valueParentRef;
              }
              return new Pair<>(parent, child);
            }

            @Override
            public void deselect(
              final Context context, final Atom self, final String string, final Common.UserData userData
            ) {
              if (self.visual != null && string.isEmpty()) {
                self.valueParentRef.replace(context, ((FieldArray) self.fields.getOpt("value")).data.get(0));
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
                new FieldArray(dataValue, ImmutableList.of(value)),
                "gap",
                new FieldPrimitive(dataGap, ""))),
        raise);
  }

  private static class SuffixGapAtom extends Atom {
    private final boolean raise;

    public SuffixGapAtom(
            final AtomType type, final TSMap<String, Field> data, final boolean raise) {
      super(type, data);
      this.raise = raise;
    }
  }
}
