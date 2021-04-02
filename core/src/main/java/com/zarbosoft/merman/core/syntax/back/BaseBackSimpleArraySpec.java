package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.values.FieldArray;
import com.zarbosoft.merman.core.document.values.FieldAtom;
import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateAtomIdNotNull;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateDuplicateType;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateMissingAtom;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateNotInBaseSet;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateOverlaps;
import com.zarbosoft.merman.core.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
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
        BackSpec boilerplate = config.boilerplate.get(i);
        final BackAtomSpec[] boilerplateAtom = {null};
        BackSpec.walk(
            boilerplate,
            child -> {
              if (!(child instanceof BackAtomSpec)) return true;
              if (((BackAtomSpec) child).id != null)
                errors.add(new ArrayBoilerplateAtomIdNotNull(((BackAtomSpec) child).type));
              if (boilerplateAtom[0] != null) {
                errors.add(new ArrayMultipleAtoms(this, boilerplateAtom[0], child));
              } else {
                boilerplateAtom[0] = (BackAtomSpec) child;
              }
              return false;
            });
        if (boilerplateAtom[0] == null) {
          errors.add(new ArrayBoilerplateMissingAtom(i));
        } else {
          if (out.putReplace(boilerplateAtom[0].type, boilerplate) != null) {
            errors.add(new ArrayBoilerplateDuplicateType(boilerplateAtom[0].type));
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
            final TSList initialValue = new TSList<>();
            store = store.popVarSingle(v -> initialValue.add(v));
            initialValue.reverse();
            return store.stackVarDoubleElement(
                id, new ROPair<>(new FieldArray(BaseBackSimpleArraySpec.this), initialValue));
          }
        });
  }

  protected void buildBackRuleInner(I18nEngine i18n, Syntax syntax, Sequence s) {
    s.add(StackStore.prepVarStack)
        .add(
            new Repeat(
                new Sequence()
                    .add(
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
                                        for (AtomType sub :
                                            syntax.splayedTypes.get(plated.getKey())) {
                                          remaining.remove(sub.id());
                                        }
                                        union.add(
                                            new Sequence()
                                                .add(plated.getValue().buildBackRule(i18n, syntax))
                                                .add(
                                                    new Operator<StackStore>() {
                                                      @Override
                                                      protected StackStore process(
                                                          StackStore store) {
                                                        // The inner atom reuses the array counter,
                                                        // reduce for when it's incremented below
                                                        int counter = store.stackTop();
                                                        store = store.popStack(); // counter
                                                        store = store.popStack(); // key
                                                        FieldAtom inner = store.stackTop();
                                                        store = store.popStack();
                                                        // Reset to pre-var-push state, with atom on
                                                        // top
                                                        return store
                                                            .pushStack(counter - 1)
                                                            .pushStack(inner.data);
                                                      }
                                                    }));
                                      }
                                      for (String unplated : remaining) {
                                        union.add(new Reference(unplated));
                                      }
                                    }))
                    .add(StackStore.pushVarStackSingle)));
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
      this.splayedBoilerplate = splayedBoilerplate;
    }
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
