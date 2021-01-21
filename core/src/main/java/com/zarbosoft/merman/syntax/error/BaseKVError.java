package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.back.BackSpecData;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseKVError extends TSMap<String, Object> {
  protected abstract String name();

  private void formatValue(
      StringBuilder out, Deque<Pair<Integer, Iterator>> stack, int indent, Object value) {
    if (value instanceof BaseKVError) {
      out.append(((BaseKVError) value).name());
      Iterator<Map.Entry<String, Object>> nextLevel = ((BaseKVError) value).iterator();
      if (nextLevel.hasNext()) stack.addLast(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof List) {
      Iterator nextLevel = ((List<?>) value).iterator();
      if (nextLevel.hasNext()) stack.addLast(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof ROList) {
      Iterator nextLevel = ((ROList<?>) value).iterator();
      if (nextLevel.hasNext()) stack.addLast(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof MultiError) {
      Iterator nextLevel = ((MultiError) value).errors.iterator();
      if (nextLevel.hasNext()) stack.addLast(new Pair<>(indent + 1, nextLevel));
    } else {
      if (value instanceof BackSpecData) {
        out.append(
            String.format("%s (%s)", ((BackSpecData) value).id, value.getClass().getSimpleName()));
      } else {
        out.append(value.toString());
      }
    }
    out.append("\n");
  }

  @Override
  public String toString() {
    Deque<Pair<Integer, Iterator>> stack = new ArrayDeque<>();
    Iterator seed = iterator();
    if (!seed.hasNext()) return "(empty)";
    stack.addLast(new Pair<>(1, seed));
    StringBuilder out = new StringBuilder();
    out.append(name());
    out.append("\n");
    while (!stack.isEmpty()) {
      Pair<Integer, Iterator> pair = stack.peekLast();
      int indent = pair.first;
      Iterator top = pair.second;
      Object next = top.next();
      if (!top.hasNext()) stack.removeLast();
      if (indent > 0) {
        for (int i = 0; i < indent - 1; ++i) out.append("  ");
        out.append("* ");
      }
      if (next instanceof Map.Entry) {
        Map.Entry<String, Object> next1 = (Map.Entry<String, Object>) next;
        out.append(next1.getKey() + ": ");
        formatValue(out, stack, indent, next1.getValue());
      } else {
        formatValue(out, stack, indent, next);
      }
    }
    return out.toString();
  }
}
