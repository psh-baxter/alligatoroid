package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.primitivepattern.Any;
import com.zarbosoft.merman.core.syntax.primitivepattern.PatternString;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class CandidateInfo {
  /** Atom/array fronts preceding the concrete key text */
  public final ROList<FrontSpec> preceding;
  /**
   * Matches the key text and puts a var double list of (string key, string primitive contents) for
   * parsed primitive front
   */
  public final Node keyGrammar;

  public final ROList<FrontSpec> keySpecs;
  /** May be null */
  public final FrontSpec following;

  public CandidateInfo(
      ROList<FrontSpec> preceding,
      Node keyGrammar,
      ROList<FrontSpec> keySpecs,
      FrontSpec following) {
    this.preceding = preceding;
    this.keyGrammar = keyGrammar;
    this.keySpecs = keySpecs;
    this.following = following;
  }

  public static CandidateInfo inspect(Environment env, AtomType candidate) {
    ROList<FrontSpec> front = candidate.front();
    TSList<FrontSpec> preceding = new TSList<>();
    Sequence keyGrammar = new Sequence();
    TSList<FrontSpec> keySpecs = new TSList<>();
    FrontSpec[] following = {null};
    new Object() {
      {
        for (int i = 0; i < front.size(); ++i) {
          FrontSpec f = front.get(i);
          if (f instanceof FrontSymbol) {
            processSymbol((FrontSymbol) f);

          } else if (f instanceof FrontArraySpecBase) {
            if (((FrontArraySpecBase) f).prefix.isEmpty()) {
              if (!keyGrammar.isEmpty()) {
                following[0] = f;
                break;
              }
              preceding.add(f);
            } else {
              for (FrontSymbol prefix : ((FrontArraySpecBase) f).prefix) {
                processSymbol(prefix);
              }
              following[0] = f;
              break;
            }

          } else if (f instanceof FrontAtomSpec) {
            if (!keyGrammar.isEmpty()) {
              following[0] = f;
              break;
            }
            preceding.add(f);

          } else if (f instanceof FrontPrimitiveSpec) {
            keySpecs.add(f);
            keyGrammar
                .add(
                    new Operator<StackStore>() {
                      @Override
                      protected StackStore process(StackStore store) {
                        return store.pushStack(f);
                      }
                    })
                .add(StackStore.prepVarStack);
            if (((FrontPrimitiveSpec) f).field.pattern != null) {
              keyGrammar.add(((FrontPrimitiveSpec) f).field.pattern.build(true));
            } else {
              keyGrammar.add(Any.repeatedAny.build(true));
            }
          }
        }
      }

      void processSymbol(FrontSymbol f) {
        keySpecs.add(f);
        if (f.gapKey != null) {
          keyGrammar.add(new PatternString(env, f.gapKey).build(false));
        } else if (f.type instanceof SymbolTextSpec) {
          keyGrammar.add(new PatternString(env, ((SymbolTextSpec) f.type).text).build(false));
        }
      }
    };
    return new CandidateInfo(preceding, keyGrammar, keySpecs, following[0]);
  }

  /**
   * Parses matched text into fields, placed in fieldsOut. The field containing the last bit of text
   * is returned.
   *
   * @param branch
   * @param fieldsOut
   * @return
   */
  public static Field extractFromGrammarMatch(StackStore branch, TSMap<String, Field> fieldsOut) {
    Field lastPrimitive = null;
    TSList<String> glyphs = new TSList<>();
    while (branch.stackTop() != null) {
      glyphs.clear();
      branch = branch.popVarSingleList(glyphs);
      glyphs.reverse();
      StringBuilder builder = new StringBuilder();
      for (String glyph : glyphs) {
        builder.append(glyph);
      }
      FrontPrimitiveSpec spec = branch.stackTop();
      branch = branch.popStack();
      FieldPrimitive field = new FieldPrimitive(spec.field, builder.toString());
      fieldsOut.put(spec.fieldId, field);
      if (lastPrimitive == null) {
        lastPrimitive = field;
      }
    }
    return lastPrimitive;
  }
}
