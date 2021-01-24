package com.zarbosoft.rendaw.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Suppress {
  private final List<Class> except = new ArrayList<>();
  private final List<Class> only = new ArrayList<>();
  private final List<Pair<Class, Runnable>> handlers = new ArrayList<>();

  public Suppress except(Class... types) {
    if (!only.isEmpty()) throw new Assertion();
    except.addAll(Arrays.asList(types));
    return this;
  }

  public Suppress only(Class... types) {
    if (!except.isEmpty()) throw new Assertion();
    only.addAll(Arrays.asList(types));
    return this;
  }

  /**
   * Run custom code when exception type is matched
   *
   * @param match
   * @param supplier
   * @return
   */
  public Suppress handle(Class match, Runnable supplier) {
    handlers.add(new Pair<>(match, supplier));
    return this;
  }

  public void go(Common.UncheckedRunnable call) {
    try {
      call.run();
    } catch (Exception e) {
      for (Pair<Class, Runnable> handler : handlers) {
        if (!handler.first.isAssignableFrom(e.getClass())) continue;
        handler.second.run();
        return;
      }
      if (except.contains(e.getClass())) throw uncheck(e);
      if (!only.isEmpty() && !only.contains(e.getClass())) throw uncheck(e);
    }
  }
}
