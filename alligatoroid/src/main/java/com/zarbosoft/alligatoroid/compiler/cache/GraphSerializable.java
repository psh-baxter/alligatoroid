package com.zarbosoft.alligatoroid.compiler.cache;

import com.zarbosoft.alligatoroid.compiler.mortar.Record;

public interface GraphSerializable {
  // Must also implement static (thistype) graphDeserialize(Record data)
  Record graphSerialize();
}
