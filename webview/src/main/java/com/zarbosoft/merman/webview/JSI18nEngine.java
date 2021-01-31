package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;
import def.js.Intl;

public class JSI18nEngine implements I18nEngine {
  private final Intl.Segmenter wordSegmenter;
  private final Intl.Segmenter glyphSegmenter;

  public JSI18nEngine(String lang) {
    wordSegmenter =
        new Intl.Segmenter(
            lang,
            new Intl.SegmenterOptions() {
              {
                granularity = "word";
              }
            });
    glyphSegmenter =
        new Intl.Segmenter(
            lang,
            new Intl.SegmenterOptions() {
              {
                granularity = "grapheme";
              }
            });
  }

  private static class Walker implements I18nEngine.Walker {
    private final TSList<Integer> segments = new TSList<>();
    private int index;

    private Walker(Intl.Segmenter segmenter, String text) {
      Intl.Segments segments0 = segmenter.segment(text);
      Intl.Segment last = null;
      for (Intl.Segment segment : segments0) {
        segments.add(segment.index);
        last = segment;
      }
      if (last != null) {
        segments.add(text.length());
      }
    }

    @Override
    public int preceding(int offset) {
      while (segments.get(index) >= offset) {
        index -= 1;
        if (index < 0) {
          index = 0;
          return I18nEngine.DONE;
        }
      }
      return segments.get(index);
    }

    @Override
    public int following(int offset) {
      while (segments.get(index) <= offset) {
        index += 1;
        if (index >= segments.size()) {
          index = segments.size() - 1;
          return I18nEngine.DONE;
        }
      }
      return segments.get(index);
    }
  }

  @Override
  public Walker glyphWalker(String s) {
    return new Walker(glyphSegmenter, s);
  }

  @Override
  public Walker wordWalker(String s) {
    return new Walker(wordSegmenter, s);
  }

  @Override
  public Walker lineWalker(String s) {
    return new Walker(wordSegmenter, s);
  }
}
