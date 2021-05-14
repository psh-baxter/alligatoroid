package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateAtomIdNotNull;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateDuplicateType;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateMissingAtom;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateNotInBaseSet;
import com.zarbosoft.merman.core.syntax.error.ArrayBoilerplateOverlaps;
import com.zarbosoft.merman.core.syntax.error.ArrayMultipleAtoms;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.EnumerateIterable;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseBackArraySpec extends BackSpecData {
  public static final String NO_BOILERPLATE_YET = "";
  /** Base array element type */
  public final String type;

  public ROMap<String, ROList<BackSpec>> boilerplate;
  /** Non-group atom type to back spec, for writing */
  public ROMap<String, ROList<BackSpec>> splayedBoilerplate;

  protected BaseBackArraySpec(Config config) {
    super(config.id);
    this.type = config.element;
    MultiError errors = new MultiError();
    {
      TSMap<String, ROList<BackSpec>> out = new TSMap<>();
      for (int i = 0; i < config.boilerplate.size(); ++i) {
        ROList<BackSpec> boilerplate = config.boilerplate.get(i);
        final BackAtomSpec[] boilerplateAtom = {null};
        for (BackSpec boilerplateSpec : boilerplate) {
          BackSpec.walk(
              boilerplateSpec,
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
        }
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
    return new Operator<ROList<AtomType.AtomParseResult>, ROList<AtomType.FieldParseResult>>(
        inner) {
      @Override
      protected ROList<AtomType.FieldParseResult> process(ROList<AtomType.AtomParseResult> value) {
        return TSList.of(
            new AtomType.ArrayFieldParseResult(id, new FieldArray(BaseBackArraySpec.this), value));
      }
    };
  }

  protected Node<ROList<AtomType.AtomParseResult>> buildBackRuleInner(
      Environment env, Syntax syntax) {
    return new Repeat<AtomType.AtomParseResult>(
        boilerplate.none()
            ? syntax.backRuleRef(type)
            : new Union<AtomType.AtomParseResult>()
                .apply(
                    union -> {
                      TSSet<String> remaining = new TSSet<>();
                      for (AtomType core : syntax.splayedTypes.get(type)) {
                        remaining.add(core.id());
                      }
                      for (Map.Entry<String, ROList<BackSpec>> plated : boilerplate) {
                        for (AtomType sub : syntax.splayedTypes.get(plated.getKey())) {
                          remaining.remove(sub.id());
                        }
                        MergeSequence<AtomType.FieldParseResult> backSeq = new MergeSequence<>();
                        for (BackSpec boilerplateSpec : plated.getValue()) {
                          backSeq.add(boilerplateSpec.buildBackRule(env, syntax));
                        }
                        union.add(
                            new Operator<
                                ROList<AtomType.FieldParseResult>, AtomType.AtomParseResult>(
                                backSeq) {
                              @Override
                              protected AtomType.AtomParseResult process(
                                  ROList<AtomType.FieldParseResult> value) {
                                AtomType.AtomParseResult out = null;
                                for (AtomType.FieldParseResult field : value) {
                                  if (WriteStateDeepDataArray.INDEX_KEY.equals(field.key)) continue;
                                  if (out != null) throw new Assertion();
                                  out = ((AtomType.AtomFieldParseResult)field).data;
                                }
                                return out;
                              }
                            });
                      }
                      for (String unplated : remaining) {
                        union.add(syntax.backRuleRef(unplated));
                      }
                    }));
  }

  @Override
  protected final Iterator<BackSpec> walkStep() {
    List<BackSpec> out = new ArrayList<>();
    for (Map.Entry<String, ROList<BackSpec>> e : boilerplate) {
      out.addAll(e.getValue().inner_());
    }
    return out.iterator();
  }

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
      overlapping.put(syntax.gap, NO_BOILERPLATE_YET);
      overlapping.put(syntax.suffixGap, NO_BOILERPLATE_YET);
      for (AtomType base : syntax.splayedTypes.get(type)) {
        overlapping.put(base, NO_BOILERPLATE_YET); // magic value
      }
      TSMap<String, ROList<BackSpec>> splayedBoilerplate = new TSMap<>();
      for (Map.Entry<String, ROList<BackSpec>> boilerplateEl : boilerplate) {
        SyntaxPath boilerplatePath = typePath.add("boilerplate").add(boilerplateEl.getKey());
        for (EnumerateIterable.El<BackSpec> el :
            new EnumerateIterable<>(boilerplateEl.getValue())) {
          el.value.finish(
              errors, syntax, boilerplatePath.add(Integer.toString(el.index)), false, false);
        }
        for (AtomType leaf : syntax.splayedTypes.get(boilerplateEl.getKey())) {
          String old = overlapping.putReplace(leaf, boilerplateEl.getKey());
          if (old == null)
            errors.add(
                new ArrayBoilerplateNotInBaseSet(typePath, boilerplateEl.getKey(), leaf.id()));
          else if (!NO_BOILERPLATE_YET.equals(old))
            errors.add(new ArrayBoilerplateOverlaps(typePath, boilerplateEl.getKey(), leaf, old));
          else splayedBoilerplate.put(leaf.id(), boilerplateEl.getValue());
        }
      }
      this.splayedBoilerplate = splayedBoilerplate;
    } else {
      this.splayedBoilerplate = ROMap.empty;
    }
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }

  public SyntaxPath getPath(final FieldArray value, final int actualIndex) {
    return value.getSyntaxPath().add(Integer.toString(actualIndex));
  }

  public FieldArray get(final TSMap<String, Field> data) {
    return (FieldArray) data.getOpt(id);
  }

  /**
   * For creating synthetic wrappers when copying data - records need {}, non-luxem arrays []
   * @param context
   * @param children
   */
  public abstract void copy(Context context, TSList<Atom> children);

  /**
   * For pasting out of synthetic wrappers - records have {}, non-luxem arrays []
   * @param context
   * @param consumer
   */
  public abstract void uncopy(Context context, Consumer<ROList<Atom>> consumer);

  public static class Config {
    public final String id;
    public final String element;
    /**
     * Back trees with null-id BackAtom specs. The Atom types must be subsets of the base array
     * element type. Use this to remove when parsing and add when writing boilerplate required to
     * place certain atoms in this location.
     *
     * <p>Inner array is for multi-back boilerplate (for simple boilerplate use 1-element arrays)
     */
    public final ROList<ROList<BackSpec>> boilerplate;

    public Config(String id, String element, ROList<ROList<BackSpec>> boilerplate) {
      this.id = id;
      this.element = element;
      this.boilerplate = boilerplate;
    }
  }
}
