package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.document.fields.FieldId;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Iterator;
import java.util.Map;

public class BackIdSpec extends BackSpecData {
  public BackIdSpec() {
    super(null);
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Terminal<BackEvent, ROList<AtomType.FieldParseResult>>() {
      @Override
      protected ROPair<Boolean, ROList<AtomType.FieldParseResult>> matches(BackEvent event) {
        if (!(event instanceof EPrimitiveEvent)) return new ROPair<>(false, null);
        int valueId;
        try {
          valueId = Integer.parseInt(((EPrimitiveEvent) event).value);
        } catch (NumberFormatException e) {
          valueId = -1;
        }
        return new ROPair<>(
            true,
            TSList.of(new AtomType.IdFieldParseResult(new FieldId(BackIdSpec.this, valueId))));
      }

      @Override
      public String toString() {
        return "ID";
      }
    };
  }

  @Override
  public void write(
          Environment env,
          TSList<WriteState> stack,
          Map<Object, Object> data,
          EventConsumer writer) {
    writer.primitive(Integer.toString((int) data.get(this)));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }
}
