package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;
import java.util.Map;

public class WriteStateRecord extends WriteState {
  private final TSMap<String, Object> data;
  private final Iterator<Map.Entry<String, BackSpec>> iterator;

  public WriteStateRecord(final TSMap<String, Object> data, final ROMap<String, BackSpec> record) {
    this.data = data;
    this.iterator = record.iterator();
  }

  @Override
  public void run(final TSList<WriteState> stack, final EventConsumer writer) {
    final Map.Entry<String, BackSpec> next = iterator.next();
    if (iterator.hasNext()) {
      stack.add(this);
    }
    writer.key(next.getKey());
    BackSpec part = next.getValue();
    part.write(stack, data, writer);
  }
}
