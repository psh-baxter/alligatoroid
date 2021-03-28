package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.editor.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.misc.MultiError;
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
      Path typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (singularRestriction) {
      errors.add(new PluralInvalidAtLocation(typePath));
    }
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence = new Sequence();
    buildBackRuleInner(syntax, sequence);
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
