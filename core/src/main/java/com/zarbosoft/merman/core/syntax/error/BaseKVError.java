package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class BaseKVError extends TSMap<String, Object> {
  protected abstract String description();

  private void formatValue(
      StringBuilder out, TSList<Pair<Integer, Iterator>> stack, int indent, Object value) {
    if (value instanceof BaseKVError) {
      out.append(((BaseKVError) value).description());
      Iterator<Map.Entry<String, Object>> nextLevel = ((BaseKVError) value).iterator();
      if (nextLevel.hasNext()) stack.add(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof List) {
      Iterator nextLevel = ((List<?>) value).iterator();
      if (nextLevel.hasNext()) stack.add(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof ROList) {
      Iterator nextLevel = ((ROList<?>) value).iterator();
      if (nextLevel.hasNext()) stack.add(new Pair<>(indent + 1, nextLevel));
    } else if (value instanceof MultiError) {
      Iterator nextLevel = ((MultiError) value).errors.iterator();
      if (nextLevel.hasNext()) stack.add(new Pair<>(indent + 1, nextLevel));
    } else {
      if (value instanceof BackSpecData) {
        out.append(
            Format.format("%s (%s)", ((BackSpecData) value).id, value.getClass().getSimpleName()));
      } else {
        out.append(value);
      }
    }
    out.append("\n");
  }

  @Override
  public String toString() {
    TSList<Pair<Integer, Iterator>> stack = new TSList<>();
    Iterator seed = iterator();
    if (!seed.hasNext()) return "(empty)";
    stack.add(new Pair<>(1, seed));
    StringBuilder out = new StringBuilder();
    out.append(description());
    out.append("\n");
    while (!stack.isEmpty()) {
      Pair<Integer, Iterator> pair = stack.last();
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
