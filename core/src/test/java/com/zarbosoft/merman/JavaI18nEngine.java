package com.zarbosoft.merman;

import com.zarbosoft.merman.core.editor.I18nEngine;

import java.text.BreakIterator;
import java.util.Locale;

public class JavaI18nEngine implements I18nEngine {
  private final Locale locale;

  public JavaI18nEngine(Locale locale) {
    this.locale = locale;
  }

  private static class Walker implements I18nEngine.Walker {
    private final BreakIterator i;

    private Walker(BreakIterator i) {
      this.i = i;
    }

    @Override
    public int preceding(int offset) {
      return i.preceding(offset);
    }

    @Override
    public int following(int offset) {
      return i.following(offset);
    }
  }

  @Override
  public I18nEngine.Walker glyphWalker(String s) {
    BreakIterator i = BreakIterator.getCharacterInstance(locale);
    i.setText(s);
    return new Walker(i);
  }

  @Override
  public I18nEngine.Walker wordWalker(String s) {
    BreakIterator i = BreakIterator.getWordInstance(locale);
    i.setText(s);
    return new Walker(i);
  }

  @Override
  public I18nEngine.Walker lineWalker(String s) {
    BreakIterator i = BreakIterator.getLineInstance(locale);
    i.setText(s);
    return new Walker(i);
  }
}
