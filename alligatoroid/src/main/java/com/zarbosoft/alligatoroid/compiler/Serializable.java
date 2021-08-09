package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;

import java.util.Map;

public interface Serializable {
  public static void serialize(Writer writer, Object object) {
    if (object instanceof Serializable) {
      ((Serializable) object).serialize(writer);
    } else if (ROMap.class.isAssignableFrom(object.getClass())) {
      writer.recordBegin();
      for (Map.Entry e : (Iterable<Map.Entry>) ((ROMap) object)) {
        writer.key((String) e.getKey());
        serialize(writer, e.getValue());
      }
      writer.recordEnd();
    } else if (ROList.class.isAssignableFrom(object.getClass())) {
      writer.arrayBegin();
      for (Object e : ((ROList) object)) {
        serialize(writer, e);
      }
      writer.arrayEnd();
    } else if (object.getClass() == String.class) {
      writer.primitive((String) object);
    } else if (object.getClass() == Integer.class) {
      writer.primitive(Integer.toString((Integer) object));
    } else throw new Assertion();
  }

  public void serialize(Writer writer);
}
