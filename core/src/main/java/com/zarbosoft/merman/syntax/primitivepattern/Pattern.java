package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.nodes.Terminal;

import java.util.ArrayList;
import java.util.List;

public abstract class Pattern {
  public static Pattern repeatedAny = new Repeat0(new Any());

  public abstract Node build(I18nEngine i18n);

  protected static class CharacterRangeTerminal extends Terminal {
    final char low;
    final char high;

    public CharacterRangeTerminal(char low, char high) {
      this.low = low;
      this.high = high;
    }

    @Override
    protected boolean matches(Event event0, Store store) {
      String v = ((CharacterEvent) event0).value;
      if (v.length() != 1) return false;
      char c = v.charAt(0);
      return c >= low && c <= high;
    }
  }

  public static List<? extends Event> splitGlyphs(I18nEngine i18n, String text) {
    I18nEngine.Walker walker = i18n.glyphWalker();
    walker.setText(text);
    List<CharacterEvent> glyphs = new ArrayList<>();
    int end = 0;
    while (true) {
      int start = end;
      end = walker.following(end);
      if (end == I18nEngine.DONE) break;
      glyphs.add(new CharacterEvent(text.substring(start, end)));
    }
    return glyphs;
  }

  public class Matcher {
    private final Grammar grammar;

    public Matcher(I18nEngine i18n) {
      grammar = new Grammar();
      grammar.add("root", build(i18n));
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
