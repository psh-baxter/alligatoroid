package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class WriteStateDeepDataArray extends WriteState {
  public static final String INDEX_KEY = "__deep_index";
  public static final String INDEX_KEY_PREFIX = "__deep_index_";
  private final Iterator<Atom> iterator;
  private final ROMap<String, ROList<BackSpec>> boilerplate;
  /** Used for creating consistent unique keys for gaps in records (hack?) */
  private int index = -1;

  public WriteStateDeepDataArray(
      final ROList<Atom> values, final ROMap<String, ROList<BackSpec>> boilerplate) {
    this.iterator = values.iterator();
    this.boilerplate = boilerplate;
  }

  @Override
  public void run(Environment env, final TSList<WriteState> stack, final EventConsumer writer) {
    if (!iterator.hasNext()) return;
    final Atom next = iterator.next();
    if (iterator.hasNext()) stack.add(this);
    index += 1;
    ROList<BackSpec> nextPlate = boilerplate.getOpt(next.type.id());
    if (nextPlate != null) {
      TSMap<String, Object> carryNext =
          new TSMap<String, Object>()
              .putNull(null, next)
              .put(INDEX_KEY, new StringBuilder(INDEX_KEY_PREFIX + index));
      for (BackSpec plateSpec : nextPlate) {
        plateSpec.write(env, stack, carryNext, writer);
      }
    } else {
      next.write(stack);
    }
  }
}
