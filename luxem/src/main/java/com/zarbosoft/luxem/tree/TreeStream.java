package com.zarbosoft.luxem.tree;

import com.zarbosoft.luxem.events.LArrayCloseEvent;
import com.zarbosoft.luxem.events.LArrayOpenEvent;
import com.zarbosoft.luxem.events.LKeyEvent;
import com.zarbosoft.luxem.events.LRecordCloseEvent;
import com.zarbosoft.luxem.events.LRecordOpenEvent;
import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.luxem.events.LTypeEvent;
import com.zarbosoft.luxem.events.LuxemEvent;
import com.zarbosoft.rendaw.common.Common;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TreeStream {
  public static Stream<LuxemEvent> stream(List tree) {
    return Common.stream(
        new Iterator<LuxemEvent>() {
          Deque stack = new ArrayDeque<>();

          {
            stack.addAll(tree);
          }

          @Override
          public boolean hasNext() {
            return !stack.isEmpty();
          }

          @Override
          public LuxemEvent next() {
            Object top = stack.removeLast();
            if (!(top instanceof LuxemEvent)) {
              expand(stack, top);
              top = stack.removeLast();
            }
            return (LuxemEvent) top;
          }
        });
  }

  private static void expand(Deque stack, Object v) {
    if (v instanceof Map) {
      stack.add(LRecordCloseEvent.instance);
      for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
        stack.add(e.getValue());
        stack.add(new LKeyEvent(e.getKey()));
      }
      stack.add(LRecordOpenEvent.instance);
    } else if (v instanceof List) {
      stack.add(LArrayCloseEvent.instance);
      stack.addAll((Collection) v);
      stack.add(LArrayOpenEvent.instance);
    } else if (v instanceof Typed) {
      stack.add(((Typed) v).value);
      stack.add(new LTypeEvent(((Typed) v).name));
    } else {
      stack.add(new LPrimitiveEvent((String) v));
    }
  }
}
