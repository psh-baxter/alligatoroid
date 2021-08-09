package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateFixedRecord;
import com.zarbosoft.merman.core.serialization.WriteStateRecordEnd;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.RecordDiscardDuplicateKey;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.pidgoon.nodes.MergeSet;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROOrderedMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Iterator;
import java.util.Map;

public class BackFixedRecordSpec extends BackSpec {

  public final ROOrderedMap<String, BackSpec> pairs;
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
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    final MergeSequence<AtomType.FieldParseResult> sequence = new MergeSequence<>();
    sequence.addIgnored(new MatchingEventTerminal<BackEvent>(new EObjectOpenEvent()));
    final MergeSet<AtomType.FieldParseResult> set = new MergeSet<>();
    for (ROPair<String, BackSpec> pair : pairs) {
      set.add(
          new MergeSequence<AtomType.FieldParseResult>()
              .addIgnored(new MatchingEventTerminal<BackEvent>(new EKeyEvent(pair.first)))
              .add(pair.second.buildBackRule(env, syntax)),
          true);
    }
    for (String key : discard) {
      set.add(
          new MergeSequence<AtomType.FieldParseResult>()
              .addIgnored(new MatchingEventTerminal<BackEvent>(new EKeyEvent(key)))
              .addIgnored(new Reference<>(Syntax.GRAMMAR_WILDCARD_KEY)),
          false);
    }
    sequence.add(set);
    sequence.addIgnored(new MatchingEventTerminal<BackEvent>(new EObjectCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
      MultiError errors,
      final Syntax syntax,
      final SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    for (ROPair<String, BackSpec> e : pairs) {
      String k = e.first;
      e.second.finish(errors, syntax, typePath.add(k), true, false);
      e.second.parent =
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
  public void write(Environment env, TSList<WriteState> stack, Map<Object, Object> data, EventConsumer writer) {
    writer.recordBegin();
    stack.add(new WriteStateRecordEnd());
    stack.add(new WriteStateFixedRecord(data, pairs));
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
    public final ROOrderedMap<String, BackSpec> pairs;
    public final ROSet<String> discard;

    public Config(ROOrderedMap<String, BackSpec> pairs, ROSet<String> discard) {
      this.pairs = pairs;
      this.discard = discard;
    }
  }
}
