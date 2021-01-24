package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.events.LArrayCloseEvent;
import com.zarbosoft.luxem.events.LArrayOpenEvent;
import com.zarbosoft.luxem.events.LKeyEvent;
import com.zarbosoft.luxem.events.LRecordCloseEvent;
import com.zarbosoft.luxem.events.LRecordOpenEvent;
import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.luxem.events.LTypeEvent;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.Common;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

/** Luxem lexer. Calls the various `eat*` methods when tokens are found. */
public abstract class Reader {
  private final Deque<State> stack = new ArrayDeque<>();
  private int offset = 0;
  public Reader() {
    stack.addLast(new RootArray());
  }

  public static Stream<Boolean> stream(final Reader reader, final InputStream source) {
    return Common.concatNull(Common.stream(source))
        .map(
            bytes -> {
              if (bytes == null) {
                // Post-last chunk
                reader.finish();
                return true;
              } else {
                for (final byte b : bytes) reader.eat(b);
                return false;
              }
            });
  }

  private void finish() {
    if (stack.size() >= 3) {
      if (stack.size() == 3 && stack.peekLast().getClass() == Primitive.class)
        stack.peekLast().finish(this);
      else throw new InvalidStream(offset, "End reached mid-element.");
    }
  }

  public void eat(final byte next) {
    while (!stack.peekLast().eat(this, next)) {}
    offset += 1;
  }

  private boolean eatInterstitial(final byte next) {
    switch (next) {
      case (byte) '\t':
        return true;
      case (byte) '\n':
        return true;
      case (byte) ' ':
        return true;
      case (byte) '*':
        stack.addLast(new Comment());
        return true;
    }
    return false;
  }

  protected abstract void eatTypeEnd();

  protected abstract void eatType(byte next);

  protected abstract void eatTypeBegin();

  protected abstract void eatRecordBegin();

  protected abstract void eatArrayBegin();

  protected abstract void eatArrayEnd();

  protected abstract void eatRecordEnd();

  protected abstract void eatKey(byte next);

  protected abstract void eatKeyBegin();

  protected abstract void eatKeyEnd();

  protected abstract void eatPrimitive(byte next);

  protected abstract void eatPrimitiveBegin();

  protected abstract void eatPrimitiveEnd();

  public interface EventFactory {
    Event objectOpen();

    Event objectClose();

    Event arrayOpen();

    Event arrayClose();

    Event key(String s);

    Event type(String s);

    Event primitive(String s);
  }

  private abstract static class State {
    public abstract boolean eat(Reader raw, byte next);

    public void finish(final Reader raw) {
      throw new InvalidStream(raw.offset, "End reached mid-element.");
    }

    protected void finished(final Reader raw) {
      raw.stack.pollLast();
    }
  }

  private static class RootArray extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      raw.stack.addLast(new RootArrayBorder());
      raw.stack.addLast(new Value());
      return false;
    }
  }

  private static class RootArrayBorder extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      switch (next) {
        case (byte) ',':
          finished(raw);
          return true;
      }
      throw new InvalidStream(raw.offset, "Expected [,].");
    }
  }

  private static class Type extends TextState {
    @Override
    public void begin(final Reader raw) {
      raw.eatTypeBegin();
    }

    @Override
    protected void end(final Reader raw) {
      raw.eatTypeEnd();
    }

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {
      raw.eatType(next);
    }

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      return next == (byte) ')' ? Resolution.ATE : Resolution.REJECTED;
    }
  }

  private static class Value extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      if (next == (byte) '(') {
        finished(raw);
        raw.stack.addLast(new UntypedValue());
        raw.stack.addLast(new Type());
        return true;
      }
      return UntypedValue.eatStatic(this, raw, next);
    }
  }

  private static class UntypedValue extends State {

    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      return eatStatic(this, raw, next);
    }

    public static boolean eatStatic(final State state, final Reader raw, final byte next) {
      state.finished(raw);
      switch (next) {
        case (byte) '[':
          raw.eatArrayBegin();
          raw.stack.addLast(new Array());
          return true;
        case (byte) '{':
          raw.eatRecordBegin();
          raw.stack.addLast(new Record());
          return true;
        case (byte) '"':
          raw.stack.addLast(new QuotedPrimitive());
          return true;
      }
      raw.stack.addLast(new Primitive());
      return false;
    }
  }

  private static class Array extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      if (next == (byte) ']') {
        raw.eatArrayEnd();
        finished(raw);
        return true;
      }
      raw.stack.addLast(new ArrayBorder());
      raw.stack.addLast(new Value());
      return false;
    }
  }

  private static class ArrayBorder extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      switch (next) {
        case (byte) ',':
          finished(raw);
          return true;
        case (byte) ']':
          finished(raw);
          return false;
      }
      throw new InvalidStream(raw.offset, "Expected [,] or []].");
    }
  }

  private static class Record extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      if (next == (byte) '}') {
        raw.eatRecordEnd();
        finished(raw);
        return true;
      }
      raw.stack.addLast(new RecordBorder());
      raw.stack.addLast(new Value());
      raw.stack.addLast(new RecordSeparator());
      raw.stack.addLast(new UndifferentiatedKey());
      return false;
    }
  }

  private static class RecordSeparator extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      if (next == (byte) ':') {
        finished(raw);
        return true;
      }
      throw new InvalidStream(raw.offset, "Expected [:].");
    }
  }

  private static class RecordBorder extends State {
    @Override
    public boolean eat(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return true;
      switch (next) {
        case (byte) ',':
          finished(raw);
          return true;
        case (byte) '}':
          finished(raw);
          return false;
      }
      throw new InvalidStream(raw.offset, "Expected [,] or [}].");
    }
  }

  private static class UndifferentiatedKey extends State {

    @Override
    public boolean eat(final Reader raw, final byte next) {
      finished(raw);
      if (raw.eatInterstitial(next)) return true;
      if (next == (byte) '"') {
        raw.stack.addLast(new QuotedKey());
        return true;
      } else {
        raw.stack.addLast(new Key());
        return false;
      }
    }
  }

  private static class QuotedKey extends TextState {
    @Override
    public void begin(final Reader raw) {
      raw.eatKeyBegin();
    }

    @Override
    protected void end(final Reader raw) {
      raw.eatKeyEnd();
    }

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {
      raw.eatKey(next);
    }

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      return next == (byte) '"' ? Resolution.ATE : Resolution.REJECTED;
    }
  }

  private static class Key extends TextState {
    @Override
    public void begin(final Reader raw) {
      raw.eatKeyBegin();
    }

    @Override
    protected void end(final Reader raw) {
      raw.eatKeyEnd();
    }

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {
      raw.eatKey(next);
    }

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return Resolution.ATE;
      if (next == (byte) ':') return Resolution.TASTED;
      return Resolution.REJECTED;
    }
  }

  private static class QuotedPrimitive extends TextState {
    @Override
    public void begin(final Reader raw) {
      raw.eatPrimitiveBegin();
    }

    @Override
    protected void end(final Reader raw) {
      raw.eatPrimitiveEnd();
    }

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {
      raw.eatPrimitive(next);
    }

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      return next == (byte) '"' ? Resolution.ATE : Resolution.REJECTED;
    }
  }

  private static class Primitive extends TextState {
    @Override
    protected void begin(final Reader raw) {
      raw.eatPrimitiveBegin();
    }

    @Override
    protected void end(final Reader raw) {
      raw.eatPrimitiveEnd();
    }

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {
      raw.eatPrimitive(next);
    }

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      if (raw.eatInterstitial(next)) return Resolution.ATE;
      switch (next) {
        case (byte) ']':
        case (byte) '}':
        case (byte) ',':
          return Resolution.TASTED;
      }
      return Resolution.REJECTED;
    }
  }

  private static class Comment extends TextState {
    @Override
    protected void begin(final Reader raw) {}

    @Override
    protected void end(final Reader raw) {}

    @Override
    protected void eatMiddle(final Reader raw, final byte next) {}

    @Override
    protected Resolution eatEnd(final Reader raw, final byte next) {
      return next == (byte) '*' ? Resolution.ATE : Resolution.REJECTED;
    }
  }

  private abstract static class TextState extends State {
    boolean first = true;
    boolean escape = false;

    @Override
    public final boolean eat(final Reader raw, final byte next) {
      if (first) {
        first = false;
        begin(raw);
      }
      if (!escape) {
        if (next == '\\') {
          escape = true;
          return true;
        }
        final Resolution ended = eatEnd(raw, next);
        if (ended != Resolution.REJECTED) {
          end(raw);
          finished(raw);
          return ended == Resolution.ATE ? true : false;
        }
      }
      if (escape && next == 'n') eatMiddle(raw, (byte) '\n');
      else if (escape && next == 'r') eatMiddle(raw, (byte) '\r');
      else if (escape && next == 't') eatMiddle(raw, (byte) '\t');
      else eatMiddle(raw, next);
      escape = false;
      return true;
    }

    @Override
    public void finish(final Reader raw) {
      end(raw);
      finished(raw);
    }

    protected abstract void begin(Reader raw);

    protected abstract void end(Reader raw);

    protected abstract void eatMiddle(Reader raw, byte next);

    protected abstract Resolution eatEnd(Reader raw, byte next);

    public enum Resolution {
      ATE,
      TASTED,
      REJECTED
    }
  }

  public static class DefaultEventFactory implements EventFactory {
    @Override
    public Event objectOpen() {
      return LRecordOpenEvent.instance;
    }

    @Override
    public Event objectClose() {
      return LRecordCloseEvent.instance;
    }

    @Override
    public Event arrayOpen() {
      return LArrayOpenEvent.instance;
    }

    @Override
    public Event arrayClose() {
      return LArrayCloseEvent.instance;
    }

    @Override
    public Event key(final String s) {
      return new LKeyEvent(s);
    }

    @Override
    public Event type(final String s) {
      return new LTypeEvent(s);
    }

    @Override
    public Event primitive(final String s) {
      return new LPrimitiveEvent(s);
    }
  }
}
