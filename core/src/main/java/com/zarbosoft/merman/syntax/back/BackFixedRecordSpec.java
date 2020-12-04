package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Set;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BackFixedRecordSpec extends BackSpec {

  public Map<String, BackSpec> pairs = new HashMap<>();

  @Override
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EObjectOpenEvent()));
    final Set set = new Set();
    pairs.forEach(
        (key, value) -> {
          set.add(
              new Sequence()
                  .add(new MatchingEventTerminal(new EKeyEvent(key)))
                  .add(value.buildBackRule(syntax)));
        });
    sequence.add(set);
    sequence.add(new MatchingEventTerminal(new EObjectCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
    List<Object> errors,
    final Syntax syntax,
    final Path typePath,
    final TSMap<String, BackSpecData> fields,
    boolean singularRestriction, boolean typeRestriction
  ) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    for (Map.Entry<String, BackSpec> e : pairs.entrySet()) {
      String k = e.getKey();
      e.getValue().finish(errors, syntax, typePath.add(k), fields, true, false);
      e.getValue().parent =
          new PartParent() {
            @Override
            public BackSpec part() {
              return BackFixedRecordSpec.this;
            }

            @Override
            public String pathSection() {
              return k;
            }
          };
    }
  }

  @Override
  public void write(
    Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.recordBegin();
    stack.addLast(new Write.WriteStateRecordEnd());
    stack.addLast(new Write.WriteStateRecord(data, pairs));
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
    return pairs.values().iterator();
  }
}
