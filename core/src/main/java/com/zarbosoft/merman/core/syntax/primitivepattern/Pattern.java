package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.List;

import static com.zarbosoft.rendaw.common.Common.isOrdered;

public abstract class Pattern {
  public abstract Node build(I18nEngine i18n);

  protected static class CharacterRangeTerminal extends Terminal {
    final String low;
    final String high;

    public CharacterRangeTerminal(String low, String high) {
      this.low = low;
      this.high = high;
    }

    @Override
    protected boolean matches(Event event0, Store store) {
      String v = ((CharacterEvent) event0).value;
      if (v.length() != 1) return false;
      return isOrdered(low, v) && isOrdered(v, high);
    }
  }

  public static List<? extends Event> splitGlyphs(I18nEngine i18n, String text) {
    I18nEngine.Walker walker = i18n.glyphWalker(text);
    TSList<String> glyphs1 = TSList.of();
    int end = 0;
    while (true) {
      int start = end;
      end = walker.following(end);
      if (end == I18nEngine.DONE) break;
      glyphs1.add(text.substring(start, end));
    }
    TSList<String> pre = glyphs1;
    List<CharacterEvent> glyphs = new ArrayList<>();
    for (String s : pre) {
      glyphs.add(new CharacterEvent(s));
    }
    return glyphs;
  }

  public static class Matcher {
    private final Grammar grammar;

    public Matcher(Pattern pattern, I18nEngine i18n) {
      grammar = new Grammar();
      grammar.add("root", pattern.build(i18n));
    }

    public boolean match(Context context, final String value) {
      try {
        new ParseBuilder<Void>().grammar(grammar).parse(splitGlyphs(context.i18n, value));
        return true;
      } catch (final InvalidStream e) {
        return false;
      }
    }
  }
}
