package com.zarbosoft.merman.syntax.format;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Format {

  public List<Element> elements;

  public String format(final Map<String, Object> data) {
    return elements.stream().map(element -> element.format(data)).collect(Collectors.joining());
  }
}
