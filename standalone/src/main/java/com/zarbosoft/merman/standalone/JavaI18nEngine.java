package com.zarbosoft.merman.standalone;

import com.zarbosoft.merman.editor.I18nEngine;

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
    public void setText(String text) {
      i.setText(text);
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
  public I18nEngine.Walker glyphWalker() {
    return new Walker(BreakIterator.getCharacterInstance(locale));
  }

  @Override
  public I18nEngine.Walker wordWalker() {
    return new Walker(BreakIterator.getWordInstance(locale));
  }

  @Override
  public I18nEngine.Walker lineWalker() {
    return new Walker(BreakIterator.getLineInstance(locale));
  }
}
