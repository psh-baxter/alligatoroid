package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateAtomIdNotNull;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateDuplicateType;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateMissingAtom;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateNotInBaseSet;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateOverlaps;
import com.zarbosoft.merman.core.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
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

  protected Node<ROList<AtomType.FieldParseResult>> buildBackRuleInnerEnd(
      Node<ROList<AtomType.AtomParseResult>> inner) {
    return new Operator<ROList<AtomType.AtomParseResult>, ROList<AtomType.FieldParseResult>>(inner) {
      @Override
      protected ROList<AtomType.FieldParseResult> process(ROList<AtomType.AtomParseResult> value) {
        return TSList.of(new AtomType.ArrayFieldParseResult(
            id, new FieldArray(BaseBackSimpleArraySpec.this), value));
      }
    };
  }

  protected Node<ROList<AtomType.AtomParseResult>> buildBackRuleInner(
      Environment env, Syntax syntax) {
    return new Repeat<AtomType.AtomParseResult>(
        boilerplate.none()
            ? new Reference<AtomType.AtomParseResult>(new AtomKey(type))
            : new Union<AtomType.AtomParseResult>()
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
                        union.add(
                            new Operator<ROList<AtomType.FieldParseResult>, AtomType.AtomParseResult>(
                                plated.getValue().buildBackRule(env, syntax)) {
                              @Override
                              protected AtomType.AtomParseResult process(ROList<AtomType.FieldParseResult> value) {
                                if (value.size() != 1) throw new Assertion();
                                return ((AtomType.AtomFieldParseResult) value.get(0)).data;
                              }
                            });
                      }
                      for (String unplated : remaining) {
                        union.add(new Reference<AtomType.AtomParseResult>(new AtomKey(unplated)));
                      }
                    }));
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
      final SyntaxPath typePath,
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
    public final ROList<BackSpec> boilerplate;

    public Config(String id, String element, ROList<BackSpec> boilerplate) {
      this.id = id;
      this.element = element;
      this.boilerplate = boilerplate;
    }
  }
}
