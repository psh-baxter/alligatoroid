package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.ArrayBoilerplateAtomIdNotNull;
import com.zarbosoft.merman.syntax.error.ArrayBoilerplateDuplicateType;
import com.zarbosoft.merman.syntax.error.ArrayBoilerplateMissingAtom;
import com.zarbosoft.merman.syntax.error.ArrayBoilerplateNotInBaseSet;
import com.zarbosoft.merman.syntax.error.ArrayBoilerplateOverlaps;
import com.zarbosoft.merman.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseBackSimpleArraySpec extends BaseBackArraySpec {
  /** Base array element type */
  public final String type;

  public ROMap<String, BackSpec> boilerplate;
  /** Non-group atom type to back spec, for writing */
  public ROMap<String, BackSpec> splayedBoilerplate;

  protected BaseBackSimpleArraySpec(Config config) {
    super(config.id);
    this.type = config.element;
    MultiError errors = new MultiError();
    {
      TSMap<String, BackSpec> out = new TSMap<>();
      for (int i = 0; i < config.boilerplate.size(); ++i) {
        BackSpec b = config.boilerplate.get(i);
        final BackAtomSpec[] atom = {null};
        BackSpec.walk(
            b,
            child -> {
              if (!(child instanceof BackAtomSpec)) return true;
              if (((BackAtomSpec) child).id != null)
                errors.add(new ArrayBoilerplateAtomIdNotNull(((BackAtomSpec) child).type));
              if (atom[0] != null) {
                errors.add(new ArrayMultipleAtoms(this, atom[0], child));
              } else {
                out.put(((BackAtomSpec) child).type, b);
                atom[0] = (BackAtomSpec) child;
              }
              return false;
            });
        if (atom[0] == null) {
          errors.add(new ArrayBoilerplateMissingAtom(i));
        } else {
          if (out.putReplace(atom[0].type, b) != null) {
            errors.add(new ArrayBoilerplateDuplicateType(atom[0].type));
          }
        }
      }
      this.boilerplate = out;
    }
    errors.raise();
  }

  protected void buildBackRuleInnerEnd(Sequence s) {
    s.add(
        new Operator<StackStore>() {
          @Override
          protected StackStore process(StackStore store) {
            final TSList<Atom> temp = new TSList<>();
            store = store.<String, ValueAtom>popVarDouble((_k, v) -> temp.add(v.data));
            temp.reverse();
            return store.stackVarDoubleElement(
                id, new ValueArray(BaseBackSimpleArraySpec.this, temp));
          }
        });
  }

  protected void buildBackRuleInner(Syntax syntax, Sequence s) {
    s.add(StackStore.prepVarStack)
        .add(
            new Repeat(
                boilerplate.none()
                    ? new Reference(type)
                    : new Union()
                        .apply(
                            union -> {
                              TSSet<String> remaining = new TSSet<>();
                              for (AtomType core : syntax.splayedTypes.get(type)) {
                                remaining.add(core.id());
                              }
                              for (Map.Entry<String, BackSpec> plated : boilerplate) {
                                for (AtomType sub : syntax.splayedTypes.get(plated.getKey())) {
                                  remaining.remove(sub.id());
                                }
                                union.add(plated.getValue().buildBackRule(syntax));
                              }
                              for (String unplated : remaining) {
                                union.add(new Reference(unplated));
                              }
                            })));
  }

  @Override
  protected final Iterator<BackSpec> walkStep() {
    List<BackSpec> out = new ArrayList<>();
    for (Map.Entry<String, BackSpec> e : boilerplate) {
      out.add(e.getValue());
    }
    return out.iterator();
  }

  @Override
  public String elementAtomType() {
    return type;
  }

  @Override
  public void finish(
      MultiError errors,
      final Syntax syntax,
      final Path typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    TSMap<AtomType, String> overlapping = new TSMap<>();
    if (boilerplate.some()) {
      for (AtomType base : syntax.splayedTypes.get(type)) {
        overlapping.put(base, ""); // magic value
      }
      TSMap<String, BackSpec> splayedBoilerplate = new TSMap<>();
      for (Map.Entry<String, BackSpec> e : boilerplate) {
        e.getValue()
            .finish(errors, syntax, typePath.add("boilerplate").add(e.getKey()), false, false);
        for (AtomType sub : syntax.splayedTypes.get(e.getKey())) {
          String old = overlapping.putReplace(sub, e.getKey());
          if (old == null)
            errors.add(new ArrayBoilerplateNotInBaseSet(typePath, e.getKey(), sub.id()));
          else if (!"".equals(old))
            errors.add(new ArrayBoilerplateOverlaps(typePath, e.getKey(), sub, old));
          else splayedBoilerplate.put(sub.id(), e.getValue());
        }
      }
    }
    this.splayedBoilerplate = splayedBoilerplate;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }

  public static class Config {
    public final String id;
    public final String element;
    /**
     * Back trees with null-id BackAtom specs. The Atom types must be subsets of the base array
     * element type. Use this to remove when parsing and add when writing boilerplate required to
     * place certain atoms in this location.
     */
    public final TSList<BackSpec> boilerplate;

    public Config(String id, String element, TSList<BackSpec> boilerplate) {
      this.id = id;
      this.element = element;
      this.boilerplate = boilerplate;
    }
  }
}
