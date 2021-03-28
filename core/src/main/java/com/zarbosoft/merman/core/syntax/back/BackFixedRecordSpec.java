package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.core.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.editor.serialization.WriteStateRecord;
import com.zarbosoft.merman.core.editor.serialization.WriteStateRecordEnd;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.RecordDiscardDuplicateKey;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Set;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;
import java.util.Map;

public class BackFixedRecordSpec extends BackSpec {

  public final ROMap<String, BackSpec> pairs;
  /**
   * Discard these keys if they appear in the data - warning! Even if you don't perform any edits
   * saving a loaded file will cause data in discard keys to be removed.
   */
  public final ROSet<String> discard;

  public BackFixedRecordSpec(Config config) {
    this.pairs = config.pairs;
    this.discard = config.discard;
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EObjectOpenEvent()));
    final Set set = new Set();
    for (Map.Entry<String, BackSpec> pair : pairs) {
      set.add(
          new Sequence()
              .add(new MatchingEventTerminal(new EKeyEvent(pair.getKey())))
              .add(pair.getValue().buildBackRule(syntax)),
          true);
    }
    for (String key : discard) {
      set.add(
          new Sequence()
              .add(new MatchingEventTerminal(new EKeyEvent(key)))
              .add(new Reference(Syntax.GRAMMAR_WILDCARD_KEY)),
          false);
    }
    sequence.add(set);
    sequence.add(new MatchingEventTerminal(new EObjectCloseEvent()));
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
    for (Map.Entry<String, BackSpec> e : pairs) {
      String k = e.getKey();
      e.getValue().finish(errors, syntax, typePath.add(k), true, false);
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
    for (String s : discard) {
      if (pairs.has(s)) errors.add(new RecordDiscardDuplicateKey(s));
    }
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.recordBegin();
    stack.add(new WriteStateRecordEnd());
    stack.add(new WriteStateRecord(data, pairs));
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
    return pairs.iterValues();
  }

  public static class Config {
    public final ROMap<String, BackSpec> pairs;
    public final ROSet<String> discard;

    public Config(ROMap<String, BackSpec> pairs, ROSet<String> discard) {
      this.pairs = pairs;
      this.discard = discard;
    }
  }
}
