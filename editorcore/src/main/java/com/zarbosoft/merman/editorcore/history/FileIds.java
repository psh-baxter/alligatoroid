package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.rendaw.common.TSList;

public class FileIds {
  private final TSList<Integer> free = new TSList<>();
  private int next;

  public void remove(int id) {
    free.add(id);
  }

  public Integer take(Integer desired) {
    if (desired != null) {
      if (desired >= next) {
        for (int i = next; i < desired; ++i) {
          free.add(i);
        }
        next = desired + 1;
        return null;
      } else {
        for (int i = 0; i < free.size(); ++i) {
          if (free.get(i) == (int) desired) {
            free.remove(i);
            return null;
          }
        }
      }
    }
    if (free.some()) {
      return free.removeLast();
    }
    return next++;
  }
}
