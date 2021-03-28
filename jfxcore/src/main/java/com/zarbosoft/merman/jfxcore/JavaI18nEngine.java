package com.zarbosoft.merman.jfxcore;

import com.zarbosoft.merman.core.editor.I18nEngine;

import java.text.BreakIterator;
import java.util.Locale;

public class JavaI18nEngine implements I18nEngine {
  private final Locale locale;

  public JavaI18nEngine(Locale locale) {
    this.locale = locale;
  }

  @Override
  public I18nEngine.Walker glyphWalker(String s) {
    return new Walker(BreakIterator.getCharacterInstance(locale), s);
  }

  @Override
  public I18nEngine.Walker wordWalker(String s) {
    return new Walker(BreakIterator.getWordInstance(locale), s);
  }

  @Override
  public I18nEngine.Walker lineWalker(String s) {
    return new Walker(BreakIterator.getLineInstance(locale), s);
  }

  private static class Walker implements I18nEngine.Walker {
    private final BreakIterator i;

    private Walker(BreakIterator i, String s) {
      this.i = i;
      i.setText(s);
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
}
