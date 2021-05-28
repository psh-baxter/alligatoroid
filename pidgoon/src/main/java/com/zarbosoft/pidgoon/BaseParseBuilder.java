package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.InvalidStreamAt;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Leaf;
import com.zarbosoft.pidgoon.model.SerialStep;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.List;

public abstract class BaseParseBuilder<O, P extends BaseParseBuilder<O, P>> {
  protected final Reference.Key<O> root;
  protected Grammar grammar;
  protected int uncertaintyLimit = 1000;

  public BaseParseBuilder(Reference.Key<O> root) {
    super();
    this.root = root;
  }

  public BaseParseBuilder(final BaseParseBuilder<O, P> other) {
    grammar = other.grammar;
    root = other.root;
    uncertaintyLimit = other.uncertaintyLimit;
  }

  public P grammar(final Grammar grammar) {
    if (this.grammar != null) throw new IllegalArgumentException("Grammar already specified");
    if (grammar == null) throw new IllegalArgumentException("Argument is null.");
    final P out = split();
    out.grammar = grammar;
    return out;
  }

  protected abstract P split();

  public P uncertainty(final int limit) {
    if (this.uncertaintyLimit != 1000)
      throw new IllegalArgumentException("Uncertainty limit already specified");
    final P out = split();
    out.uncertaintyLimit = limit;
    return out;
  }

  /**
   * Parse by pulling events from the stream.
   *
   * @param data
   * @return
   */
  public O parallelParse(final ROList<? extends ROPair> data) {
    ParseEventSink<O> eventStream = parallelParse();
    for (ROPair pair : data) {
      eventStream = eventStream.push(pair.first, pair.second.toString());
    }
    return eventStream.result();
  }

  /**
   * Instead of pulling from an input stream, use the returned EventStream to push events to the
   * parse.
   *
   * @return
   */
  public ParseEventSink<O> parallelParse() {
    return new ParseEventSink<>(grammar, root, uncertaintyLimit);
  }

  /**
   *
   * @param data first is event, second is position
   * @return
   */
  public O serialParsePosition(ROList<? extends ROPair> data) {
    int longestIndex = -1;
    Object longestPosition = null;
    SerialStep<O> longestStep = null;
    TSList<ROPair<Integer, SerialStep<O>>> stack =
            new TSList<>(new ROPair<>(0, Pidgoon.prepare(grammar, root, new SerialStep<>())));
    while (stack.some()) {
      ROPair<Integer, SerialStep<O>> source = stack.last();
      SerialStep<O> sourceStep = source.second;
      int sourceIndex = source.first;

      if (sourceIndex >= data.size()) {
        if (sourceStep.completed.some()) return sourceStep.completed.get(0);
        stack.removeLast();
        continue;
      }

      Leaf nextLeaf = sourceStep.nextLeaf();
      if (nextLeaf == null) {
        stack.removeLast();
        continue;
      }

      SerialStep<O> resultStep;
      if (sourceIndex == longestIndex) {
        resultStep = longestStep;
      } else {
        resultStep = new SerialStep<>();
      }
      ROPair event = data.get(sourceIndex);
      if (sourceIndex >= longestIndex) {
        longestIndex = sourceIndex;
        longestStep = resultStep;
        longestPosition = event.second;
      }

      nextLeaf.parse(grammar, resultStep, event.first);

      stack.add(new ROPair<>(sourceIndex + 1, resultStep));
    }
    throw new InvalidStreamAt(longestPosition, new InvalidStream(longestStep));
  }

}
