package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.Constructor;

public class ObjectInfo {
  public final String luxemType;
  public Constructor constructor;
  public ROMap<String, StatePrototype> fields;
  public TSMap<String, Integer> argOrder;

  public ObjectInfo(String luxemType) {
    this.luxemType = luxemType;
  }
}
