package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.I18nEngine;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.PluralInvalidAtLocation;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
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
  public Node buildBackRule(I18nEngine i18n, final Syntax syntax) {
    final Sequence sequence = new Sequence();
    buildBackRuleInner(i18n, syntax, sequence);
    buildBackRuleInnerEnd(sequence);
    return sequence;
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
