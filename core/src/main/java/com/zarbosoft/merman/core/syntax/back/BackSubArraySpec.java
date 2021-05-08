package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.PluralInvalidAtLocation;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class BackSubArraySpec extends BaseBackSimpleArraySpec {

  public BackSubArraySpec(Config config) {
    super(config);
  }

  @Override
  public void finish(
      MultiError errors,
      Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (singularRestriction) {
      errors.add(new PluralInvalidAtLocation(typePath));
    }
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return buildBackRuleInnerEnd(buildBackRuleInner(env, syntax));
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    stack.add(new WriteStateDeepDataArray(((TSList<Atom>) data.get(id)), splayedBoilerplate));
  }

  @Override
  protected boolean isSingularValue() {
    return false;
  }
}
