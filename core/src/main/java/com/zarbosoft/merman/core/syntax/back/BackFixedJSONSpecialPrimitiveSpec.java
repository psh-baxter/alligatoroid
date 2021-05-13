package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.BackElementUnsupportedInBackFormat;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Discard;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackFixedJSONSpecialPrimitiveSpec extends BackSpec {
  public final String value;

  public BackFixedJSONSpecialPrimitiveSpec(String value) {
    this.value = value;
  }

  @Override
  public void finish(MultiError errors, Syntax syntax, SyntaxPath typePath, boolean singularRestriction, boolean typeRestriction) {
    if (syntax.backType != BackType.JSON) {
      errors.add(new BackElementUnsupportedInBackFormat("json special primitive", syntax.backType, typePath));
    }
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Discard<AtomType.FieldParseResult>(
        new MatchingEventTerminal<BackEvent>(new JSpecialPrimitiveEvent(value)));
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.jsonSpecialPrimitive(value);
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
  protected Iterator<BackSpec> walkStep() {
    return null;
  }
}
