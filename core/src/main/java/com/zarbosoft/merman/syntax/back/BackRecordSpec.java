package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.RecordChildMissingValue;
import com.zarbosoft.merman.syntax.error.RecordChildNotKeyAt;
import com.zarbosoft.merman.syntax.error.RecordChildNotValueAt;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackRecordSpec extends BaseBackArraySpec {
  /** Type/group name or null; null means any type */
  public final String element;

  public static class Config {
    public final String id;
    public final String element;

    public Config(String id, String element) {
      this.id = id;
      this.element = element;
    }
  }

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
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EObjectOpenEvent()));
    sequence.add(StackStore.prepVarStack);
    sequence.add(
        new Repeat(new Sequence().add(new Reference(element)).add(StackStore.pushVarStackSingle)));
    sequence.add(new MatchingEventTerminal(new EObjectCloseEvent()));
    return new Operator<StackStore>(sequence) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.popVarSingleList(temp);
        Collections.reverse(temp);
        final ValueArray value = new ValueArray(BackRecordSpec.this, temp);
        return store.stackVarDoubleElement(id, value);
      }
    };
  }

  @Override
  public void write(
      Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.recordBegin();
    stack.addLast(new Write.WriteStateRecordEnd());
    stack.addLast(new Write.WriteStateDataArray(((List<Atom>) data.get(id))));
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
      Path typePath,
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
}
