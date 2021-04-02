package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.editor.serialization.WriteStateArrayEnd;
import com.zarbosoft.merman.core.editor.serialization.WriteStateBack;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackFixedArraySpec extends BackSpec {
  public List<BackSpec> elements = new ArrayList<>();

  @Override
  protected Iterator<BackSpec> walkStep() {
    return elements.iterator();
  }

  @Override
  public Node buildBackRule(I18nEngine i18n, final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EArrayOpenEvent()));
    for (final BackSpec element : elements) {
      sequence.add(element.buildBackRule(i18n, syntax));
    }
    sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
          MultiError errors,
          final Syntax syntax,
          final Path typePath,
          boolean singularRestriction,
          boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    for (int i = 0; i < elements.size(); ++i) {
      BackSpec element = elements.get(i);
      element.finish(errors, syntax, typePath.add(Integer.toString(i)), false, false);
      int finalI = i;
      element.parent =
          new PartParent() {
            @Override
            public BackSpec part() {
              return BackFixedArraySpec.this;
            }

            @Override
            public String pathSection() {
              return Integer.toString(finalI);
            }
          };
    }
  }

  @Override
  public void write(
          TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.arrayBegin();
    stack.add(new WriteStateArrayEnd());
    stack.add(new WriteStateBack(data, elements.iterator()));
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
