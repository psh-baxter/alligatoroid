package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.serialization.WriteStateRecordEnd;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.RecordChildMissingValue;
import com.zarbosoft.merman.core.syntax.error.RecordChildNotKeyAt;
import com.zarbosoft.merman.core.syntax.error.RecordChildNotValueAt;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Map;
import java.util.function.Consumer;

public class BackRecordSpec extends BaseBackArraySpec {
  public BackRecordSpec(BaseBackArraySpec.Config config) {
    super(config);
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return buildBackRuleInnerEnd(
        new UnitSequence<ROList<AtomType.AtomParseResult>>()
            .addIgnored(new MatchingEventTerminal(new EObjectOpenEvent()))
            .add(buildBackRuleInner(env, syntax))
            .addIgnored(new MatchingEventTerminal(new EObjectCloseEvent())));
  }

  @Override
  public void write(Environment env, TSList<WriteState> stack, Map<Object, Object> data, EventConsumer writer) {
    writer.recordBegin();
    stack.add(new WriteStateRecordEnd());
    stack.add(writeContents((TSList<Atom>) data.get(id)));
  }

  private WriteState writeContents(ROList<Atom> atoms) {
    return new WriteStateDeepDataArray(atoms, splayedBoilerplate);
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  public void finish(
      MultiError errors,
      final Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    boilerplate =
        boilerplate
            .mut()
            .put(
                syntax.gap.id,
                new TSList<>(
                    new BackKeySpec(
                        new BaseBackPrimitiveSpec.Config(WriteStateDeepDataArray.INDEX_KEY)
                            .pattern(syntax.gapInRecordKeyPrefixPattern, "gap-in-record key")),
                    new BackAtomSpec(new BaseBackAtomSpec.Config(null, syntax.gap.id))))
            .put(
                syntax.suffixGap.id,
                new TSList<>(
                    new BackKeySpec(
                        new BaseBackPrimitiveSpec.Config(WriteStateDeepDataArray.INDEX_KEY)
                            .pattern(syntax.gapInRecordKeyPrefixPattern, "gap-in-record key")),
                    new BackAtomSpec(new BaseBackAtomSpec.Config(null, syntax.suffixGap.id))));
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    for (final AtomType element : syntax.splayedTypes.get(type)) {
      CheckBackState state = CheckBackState.KEY;
      for (int i = 0; i < element.back().size(); ++i) {
        BackSpec back = element.back().get(i);
        switch (state) {
          case KEY:
            {
              if (!(back instanceof BackKeySpec)) {
                errors.add(new RecordChildNotKeyAt(typePath, element, i, back));
                break;
              }
              state = CheckBackState.TYPEVALUE;
              break;
            }
          case TYPEVALUE:
            {
              if (back.isSingularValue()) {
                state = CheckBackState.KEY;
              } else {
                errors.add(new RecordChildNotValueAt(typePath, element, i, back));
                break;
              }
              break;
            }
        }
      }
      if (state != CheckBackState.KEY) {
        errors.add(new RecordChildMissingValue(typePath, element));
      }
    }
  }

  @Override
  public void copy(Context context, TSList<Atom> children) {
    context.copy(Context.CopyContext.RECORD, new TSList<>(writeContents(children)));
  }

  @Override
  public void uncopy(Context context, Consumer<ROList<Atom>> consumer) {
    context.uncopy(
        buildBackRuleInner(context.env, context.syntax), Context.CopyContext.RECORD, consumer);
  }

  public static enum CheckBackState {
    KEY,
    TYPEVALUE,
  }

  public static class Config {
    public final String id;
    public final String element;

    public Config(String id, String element) {
      this.id = id;
      this.element = element;
    }
  }
}
