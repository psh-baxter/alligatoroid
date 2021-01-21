package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROMap;

import java.util.Map;
import java.util.stream.Collectors;

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
