package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.Set;

public class BackFixedTypeSpec extends BackSpec {
  public String type;

  public BackSpec value;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Sequence()
        .add(new MatchingEventTerminal(new ETypeEvent(type)))
        .add(value.buildBackRule(syntax, atomType));
  }

  @Override
  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    super.finish(syntax, atomType, middleUsed);
    value.finish(syntax, atomType, middleUsed);
    value.parent =
        new PartParent() {
          @Override
          public BackSpec part() {
            return BackFixedTypeSpec.this;
          }

          @Override
          public String pathSection() {
            return null;
          }
        };
  }
}
