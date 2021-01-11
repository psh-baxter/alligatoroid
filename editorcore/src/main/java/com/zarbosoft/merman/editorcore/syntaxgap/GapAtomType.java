package com.zarbosoft.merman.editorcore.syntaxgap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontFixedArraySpec;
import com.zarbosoft.merman.syntax.front.FrontGapBase;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GapAtomType extends AtomType {
  private final BaseBackPrimitiveSpec gapSpec;
  private final List<BackSpec> back;
  public List<FrontSymbol> frontPrefix = new ArrayList<>();
  public List<FrontSymbol> frontSuffix = new ArrayList<>();
  private List<FrontSpec> front;

  public GapAtomType() {
    gapSpec = new BackPrimitiveSpec();
    gapSpec.id = "gap";
    final BackFixedTypeSpec backType = new BackFixedTypeSpec();
    backType.type = "__gap";
    backType.value = gapSpec;
    back = ImmutableList.of(backType);
  }

  public Value findSelectNext(final Context context, final Atom atom, boolean skipFirstNode) {
    if (atom.type == context.syntax.gap
        || atom.type == context.syntax.prefixGap
        || atom.type == context.syntax.suffixGap) return atom.fields.getOpt("gap");
    for (final FrontSpec front : atom.type.front()) {
      if (front instanceof FrontPrimitiveSpec) {
        return atom.fields.getOpt(((FrontPrimitiveSpec) front).field);
      } else if (front instanceof FrontGapBase) {
        return atom.fields.getOpt(front.field());
      } else if (front instanceof FrontAtomSpec) {
        if (skipFirstNode) {
          skipFirstNode = false;
        } else {
          final Value found =
              findSelectNext(
                  context,
                  ((ValueAtom) atom.fields.getOpt(((FrontAtomSpec) front).middle)).get(),
                  skipFirstNode);
          if (found != null) return found;
        }
      } else if (front instanceof FrontFixedArraySpec) {
        final ValueArray array =
            (ValueArray) atom.fields.getOpt(((FrontFixedArraySpec) front).middle);
        if (array.data.isEmpty()) {
          if (skipFirstNode) {
            skipFirstNode = false;
          } else {
            final Value found =
                findSelectNext(context, array.createAndAddDefault(context, 0), skipFirstNode);
            if (found != null) return found;
            else return array;
          }
        } else
          for (final Atom element : array.data) {
            if (skipFirstNode) {
              skipFirstNode = false;
            } else {
              final Value found = findSelectNext(context, element, skipFirstNode);
              if (found != null) return found;
            }
          }
      }
    }
    return null;
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
  public static class GapChoice extends TwoColumnChoice {
    private final FreeAtomType type;
    private final GapKey key;
    private final Atom self;

    GapChoice(Atom self, final FreeAtomType type, final GapKey key) {
      this.self = self;
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
      final String remainder = parsed.remainder;
      Atom root = atom;
      final Value.Parent rootPlacement = self.parent;

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

      // Place the atom
      rootPlacement.replace(context, root);

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
      return type == ((GapChoice) obj).type;
    }
  }

  @Override
  public void finish(List<Object> errors, final Syntax syntax) {
    final FrontGapBase gap =
        new FrontGapBase() {
          @Override
          public void deselect(
            final Context context, final Atom self, final String string
          ) {
            if (!string.isEmpty()) return;
            if (self.parent == null) return;
            final Value parentValue = self.parent.value;
            if (parentValue instanceof ValueArray) {
              self.parent.deleteChild(context);
            }
          }
        };
    front = ImmutableList.copyOf(Iterables.concat(frontPrefix, ImmutableList.of(gap), frontSuffix));
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
  public String name() {
    return "Gap";
  }

  @Override
  public String id() {
    return "__gap";
  }

  public Atom create() {
    return new Atom(this, new TSMap<>(ImmutableMap.of("gap", new ValuePrimitive(gapSpec, ""))));
  }
}
