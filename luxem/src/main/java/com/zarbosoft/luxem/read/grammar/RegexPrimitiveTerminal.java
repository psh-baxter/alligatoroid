package com.zarbosoft.luxem.read.grammar;

import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.Terminal;

import java.util.regex.Pattern;

public class RegexPrimitiveTerminal extends Terminal {
  public static final RegexPrimitiveTerminal integerTerminal = new RegexPrimitiveTerminal("-?\\d+");
  public static final RegexPrimitiveTerminal posIntegerTerminal =
      new RegexPrimitiveTerminal("\\d+");
  public static final RegexPrimitiveTerminal decimalTerminal =
      new RegexPrimitiveTerminal("\\d+(\\.\\d+)?");
  private final Pattern pattern;

  public RegexPrimitiveTerminal(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  @Override
  protected boolean matches(Event event, Store store) {
    if (event.getClass() != LPrimitiveEvent.class) return false;
    return pattern.matcher(((LPrimitiveEvent) event).value).find();
  }
}
