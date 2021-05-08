package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateDataArray;
import com.zarbosoft.merman.core.serialization.WriteStateRecordEnd;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.RecordChildMissingValue;
import com.zarbosoft.merman.core.syntax.error.RecordChildNotKeyAt;
import com.zarbosoft.merman.core.syntax.error.RecordChildNotValueAt;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackRecordSpec extends BaseBackArraySpec {
  /** Type/group name or null; null means any type */
  public final String element;

  public BackRecordSpec(Config config) {
    super(config.id);
    this.element = config.element;
  }

  @Override
  public String elementAtomType() {
    return element;
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Operator<ROList<AtomType.AtomParseResult>, ROList<AtomType.FieldParseResult>>(
        new UnitSequence<ROList<AtomType.AtomParseResult>>()
            .addIgnored(new MatchingEventTerminal<BackEvent>(new EObjectOpenEvent()))
            .add(
                new Repeat<AtomType.AtomParseResult>(
                    new Reference<AtomType.AtomParseResult>(new AtomKey(element))))
            .addIgnored(new MatchingEventTerminal<BackEvent>(new EObjectCloseEvent()))) {
      @Override
      protected ROList<AtomType.FieldParseResult> process(ROList<AtomType.AtomParseResult> value) {
        return TSList.of(new AtomType.ArrayFieldParseResult(id, new FieldArray(BackRecordSpec.this), value));
      }
    };
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.recordBegin();
    stack.add(new WriteStateRecordEnd());
    stack.add(new WriteStateDataArray(((TSList<Atom>) data.get(id))));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }

  @Override
  public void finish(
      MultiError errors,
      final Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    for (final AtomType element : syntax.splayedTypes.get(element)) {
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
