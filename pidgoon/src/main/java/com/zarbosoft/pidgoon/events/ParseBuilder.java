package com.zarbosoft.pidgoon.events;

import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.Pidgoon;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROPair;

import java.util.List;

public class ParseBuilder<O> extends BaseParseBuilder<ParseBuilder<O>> {
  protected ParseBuilder(final ParseBuilder<O> other) {
    super(other);
  }

  public ParseBuilder() {}

  /**
   * Parse by pulling events from the list
   *
   * @param data
   * @return
   */
  public O parse(final List<? extends Event> data) {
    ParseEventSink<O> eventStream = parse();
    for (int i = 0; i < data.size(); ++i) {
      eventStream = eventStream.push(data.get(i), i);
    }
    return eventStream.result();
  }

  public O parsePosition(final List<? extends ROPair<? extends Event, ?>> data) {
    ParseEventSink<O> eventStream = parse();
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
  public ParseEventSink<O> parse() {
    final Store store = initialStore == null ? new StackStore() : initialStore;
    return new ParseEventSink<>(
        grammar, root, store, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
  }

  /**
   * Parse until the stream stops matching.
   *
   * @param stream
   * @return First - state at last matching position; second - position of element at last match/if
   *     integer, offset into events (-1 if no match, 0 if matched at element 0, ...): max value is
   *     length of stream - 1
   */
  public Pair<Parse, Position> longestMatchFromStart(final List<Event> events) {
    final Store store = initialStore == null ? new StackStore() : initialStore;
    Parse context =
        Pidgoon.prepare(grammar, root, store, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
    Pair<Parse, Position> record = new Pair<>(context, new Position(null, -1));
    for (int i = 0; i < events.size(); ++i) {
      Event event = events.get(i);
      Position position = new Position(event, i);
      try {
        context = Pidgoon.step(context, position);
      } catch (final InvalidStream e) {
        break;
      }
      if (context == null) break;
      record = new Pair<>(context, position);
      if (context.leaves.isEmpty()) break;
    }
    return record;
  }

  @Override
  protected ParseBuilder<O> split() {
    return new ParseBuilder<>(this);
  }
}
