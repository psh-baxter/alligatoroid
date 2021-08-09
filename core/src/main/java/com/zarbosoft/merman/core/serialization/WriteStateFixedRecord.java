package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROOrderedMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;
import java.util.Map;

public class WriteStateFixedRecord extends WriteState {
  private final Map<Object, Object> data;
  private final Iterator<ROPair<String, BackSpec>> iterator;

  public WriteStateFixedRecord(
          final Map<Object, Object> data, final ROOrderedMap<String, BackSpec> record) {
    this.data = data;
    this.iterator = record.iterator();
  }

  @Override
  public void run(Environment env, final TSList<WriteState> stack, final EventConsumer writer) {
    if (!iterator.hasNext()) return;
    final ROPair<String, BackSpec> next = iterator.next();
    if (iterator.hasNext()) {
      stack.add(this);
    }
    writer.key(next.first);
    BackSpec part = next.second;
    part.write(env, stack, data, writer);
  }
}
