package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.Pidgoon;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class ParseBuilder<J> extends BaseParseBuilder<J, ParseBuilder<J>> {
  public ParseBuilder(Reference.Key<J> root) {
    super(root);
  }

  protected ParseBuilder(final ParseBuilder<J> other) {
    super(other);
  }

  /**
   * Parse by pulling events from the list
   *
   * @param data
   * @return
   */
  public J parse(final TSList<? extends Event> data) {
    ParseEventSink<J> eventStream = parse();
    for (int i = 0; i < data.size(); ++i) {
      eventStream = eventStream.push(data.get(i), i);
    }
    return eventStream.result();
  }

  public J parsePosition(final TSList<? extends ROPair<? extends Event, ?>> data) {
    ParseEventSink<J> eventStream = parse();
    for (ROPair<? extends Event, ?> pair : data) {
      eventStream = eventStream.push(pair.first, pair.second);
    }
    return eventStream.result();
  }

  /**
   * Instead of pulling from an input stream, use the returned EventStream to push events to the
   * parse.
   *
   * @return
   */
  public ParseEventSink<J> parse() {
    return new ParseEventSink<>(grammar, root, uncertaintyLimit);
  }

  /**
   * Parse until the stream stops matching.
   *
   * @param stream
   * @return First - state at last matching position; second - position of element at last match/if
   *     integer, offset into events (-1 if no match, 0 if matched at element 0, ...): max value is
   *     length of stream - 1
   */
  public Pair<Step<J>, Position> longestMatchFromStart(final TSList<Event> events) {
    Step<J> context = Pidgoon.prepare(grammar, root);
    Pair<Step<J>, Position> record = new Pair<>(context, new Position(null, -1));
    for (int i = 0; i < events.size(); ++i) {
      Event event = events.get(i);
      Position position = new Position(event, i);
      try {
        context = Pidgoon.step(grammar, uncertaintyLimit, context, event);
      } catch (final InvalidStream e) {
        break;
      }
      if (context == null) break;
      record = new Pair<>(context, position);
      if (context.branches.isEmpty()) break;
    }
    return record;
  }

  @Override
  protected ParseBuilder<J> split() {
    return new ParseBuilder<>(this);
  }
}
