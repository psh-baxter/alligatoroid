package com.zarbosoft.merman.editorcore.format;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;

public class Format {

  public final ROList<Element> elements;

  public Format(ROList<Element> elements) {
    this.elements = elements;
  }

  public String format(final ROMap<String, Object> data) {
    StringBuilder builder = new StringBuilder();
    for (Element element : elements) {
      builder.append(element.format(data));
    }
    return builder.toString();
  }
}
