package com.zarbosoft.pidgoon.events.stores;

import com.zarbosoft.pidgoon.Position;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.internal.BranchingStack;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.TSList;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StackStore extends Store {
  public static Operator<StackStore> prepVarStack =
      new Operator<StackStore>() {
        @Override
        protected StackStore process(StackStore store) {
          return store.pushStack(0);
        }
      };
  public static Operator<StackStore> pushVarStackSingle =
      new Operator<StackStore>() {
        @Override
        protected StackStore process(StackStore store) {
          return store.stackSingleElement();
        }
      };
  private Event top;
  private BranchingStack<Object> stack;

  public StackStore() {
    super(null);
    top = null;
    stack = null;
  }

  public StackStore(final Object color, final BranchingStack<Object> stack, final Event top) {
    super(color);
    this.top = top;
    this.stack = stack;
    this.color = color;
  }

  /**
   * count element must already be on stack
   *
   * @return
   */
  public StackStore stackSingleElement() {
    StackStore store = this;
    Object value = store.stackTop();
    store = store.popStack();
    return store.stackSingleElement(value);
  }

  public StackStore stackSingleElement(Object value) {
    StackStore store = this;
    final int length = store.stackTop();
    store = store.popStack();
    return store.pushStack(value).pushStack(length + 1);
  }

  public StackStore pushStack(final Object o) {
    final StackStore out = new StackStore();
    if (stack == null) {
      out.stack = new BranchingStack<>(o);
    } else {
      out.stack = stack.push(o);
    }
    return out;
  }

  public StackStore popStack() {
    final StackStore out = split();
    out.stack = stack.pop();
    return out;
  }

  @Override
  public <Y> Y split() {
    return (Y) new StackStore(color, stack, top);
  }

  @Override
  public boolean hasResult() {
    return (stack != null) && stack.isLast();
  }

  @Override
  public Object result() {
    return stackTop();
  }

  @Override
  public Store record(final Position position) {
    final StackStore out = split();
    out.top = ((com.zarbosoft.pidgoon.events.Position) position).get();
    return out;
  }

  /**
   * The last event matched.
   *
   * @param <T>
   * @return
   */
  public <T> T stackTop() {
    return (T) stack.top();
  }

  public StackStore stackVarDoubleElement() {
    StackStore store = this;
    final int length = store.stackTop();
    store = store.popStack();
    return store.pushStack(length + 1);
  }

  public StackStore stackVarDoubleElement(Object key, Object value) {
    StackStore store = this;
    final int length = store.stackTop();
    store = store.popStack();
    return store.pushStack(value).pushStack(key).pushStack(length + 1);
  }

  public StackStore stackVarDoubleElement(final Object key) {
    StackStore store = this;
    final int length = store.stackTop();
    store = store.popStack();
    return store.pushStack(key).pushStack(length + 1);
  }

  public StackStore stackFixedDoubleElement(final Object key) {
    StackStore store = this;
    return store.pushStack(key);
  }

  public <L, R> StackStore popFixedMap(final int length, Map<L, R> dest) {
    return popFixedDoubleList(length, (L l, R r) -> dest.put(l, r));
  }

  public <L, R> StackStore popFixedDoubleList(final int length, final BiConsumer<L, R> callback) {
    StackStore s = this;
    for (int i = 0; i < length; ++i) {
      final Object l = s.stackTop();
      s = s.popStack();
      final Object r = s.stackTop();
      s = s.popStack();
      callback.accept((L) l, (R) r);
    }
    return s;
  }

  public <L, R> StackStore popVarMap(Map<L, R> dest) {
    return popVarDouble((L l, R r) -> dest.put(l, r));
  }

  public <L, R> StackStore popVarDouble(final BiConsumer<L, R> callback) {
    StackStore s = this;
    final Integer count = s.stackTop();
    s = s.popStack();
    return s.popFixedDoubleList(count, callback);
  }

  public <T> StackStore popVarSingle(Consumer<T> consumer) {
    StackStore s = this;
    final Integer count = s.stackTop();
    s = s.popStack();
    return s.popFixedSingle(count, consumer);
  }

  public <T> StackStore popFixedSingle(final int length, Consumer<T> consumer) {
    StackStore s = this;
    for (int i = 0; i < length; ++i) {
      consumer.accept(s.stackTop());
      s = s.popStack();
    }
    return s;
  }

  public <T> StackStore popVarSingleList(TSList<T> out) {
    StackStore s = this;
    final Integer count = s.stackTop();
    s = s.popStack();
    return s.popFixedSingleList(count, out);
  }

  public <T> StackStore popFixedSingleList(final int length, TSList<T> out) {
    StackStore s = this;
    for (int i = 0; i < length; ++i) {
      out.add(s.stackTop());
      s = s.popStack();
    }
    return s;
  }

  public Event top() {
    return top;
  }
}
