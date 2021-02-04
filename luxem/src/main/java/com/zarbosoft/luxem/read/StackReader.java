package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.tree.Typed;
import com.zarbosoft.rendaw.common.Assertion;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackReader {
  private final Deque<State> stack = new ArrayDeque<>();

  public List read(final InputStream stream, State top) {
    stack.addLast(top);
    final BufferedReader reader =
        new BufferedReader() {

          @Override
          protected void eatRecordBegin() {
            stack.addLast(stack.peekLast().record());
          }

          @Override
          protected void eatArrayBegin() {
            stack.addLast(stack.peekLast().array());
          }

          @Override
          protected void eatArrayEnd() {
            eatRecordEnd(); // reuse
          }

          @Override
          protected void eatRecordEnd() {
            final State done = stack.pollLast();
            stack.peekLast().value(done.get());
          }

          @Override
          protected void eatType(byte[] b) {
            stack.peekLast().type(new String(b, StandardCharsets.UTF_8));
          }

          @Override
          protected void eatKey(byte[] b) {
            stack.peekLast().key(new String(b, StandardCharsets.UTF_8));
          }

          @Override
          protected void eatPrimitive(byte[] b) {
            stack.peekLast().value(new String(b, StandardCharsets.UTF_8));
          }
        };
    Reader.feed(reader, stream);
    return (List) top.get();
  }

  public abstract static class State {

    /**
     * Called after a key: element. Never called in an array context, always called before
     * type/value in a record context.
     *
     * @param value
     */
    public void key(final String value) {
      throw new Assertion();
    }

    /**
     * Called after a primitive, array, or record. The value is the result of get() from the pushed
     * state for an array or primitive, otherwise it's a string.
     *
     * @param value
     */
    public abstract void value(Object value);

    /**
     * Called after a (type) element. Never called twice before a value.
     *
     * @param value
     */
    public abstract void type(String value);

    /**
     * Called when [ is encountered
     *
     * @return
     */
    public State array() {
      return new ArrayState();
    }

    /**
     * Called when { is encountered
     *
     * @return
     */
    public State record() {
      return new RecordState();
    }

    /**
     * Called when the array/record end is encountered, used as a value() parameter in the parent
     * state.
     *
     * @return
     */
    public abstract Object get();
  }

  public static class ArrayState extends State {
    protected final List data = new ArrayList();
    protected String type = null;

    @Override
    public void value(final Object value) {
      if (type != null) {
        data.add(new Typed(type, value));
        type = null;
      } else data.add(value);
    }

    @Override
    public void type(final String value) {
      type = value;
    }

    @Override
    public Object get() {
      return data;
    }
  }

  public static class RecordState extends State {
    protected final Map data = new HashMap();
    protected String key = null;
    protected String type = null;

    @Override
    public void key(final String value) {
      key = value;
    }

    @Override
    public void value(final Object value) {
      if (type != null) {
        data.put(key, new Typed(type, value));
        type = null;
      } else data.put(key, value);
    }

    @Override
    public void type(final String value) {
      type = value;
    }

    @Override
    public Object get() {
      return data;
    }
  }
}
