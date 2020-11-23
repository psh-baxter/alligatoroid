package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.backevents.JTrueEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;

public class BackFixedJSONTrueSpec extends BackSpec {

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new MatchingEventTerminal(new JTrueEvent());
  }
}
