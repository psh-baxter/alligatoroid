package com.zarbosoft.pidgoon.errors;

import com.zarbosoft.pidgoon.parse.Parse;

import java.util.stream.Collectors;

/** There were too many branches when parsing. This is configurable in the Parse. */
public class GrammarTooUncertain extends RuntimeException {
  public final Object position;
  public final Parse context;

  public GrammarTooUncertain(final Parse context, final Object position) {
    this.context = context;
    this.position = position;
  }

  @Override
  public String toString() {
    return String.format(
        "Grammar too uncertain (%d possible next states) at:\n%s\n%s",
        context.leaves.size(),
        position,
        context.leaves.stream().map(l -> l.toString()).collect(Collectors.joining("\n")));
  }
}
