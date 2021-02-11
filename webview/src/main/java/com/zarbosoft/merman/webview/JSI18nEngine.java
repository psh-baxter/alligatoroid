package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.webview.compat.intl.Segment;
import com.zarbosoft.merman.webview.compat.intl.Segmenter;
import com.zarbosoft.rendaw.common.TSList;
import elemental2.core.JsArray;
import jsinterop.base.JsPropertyMap;

public class JSI18nEngine implements I18nEngine {
  private final Segmenter wordSegmenter;
  private final Segmenter glyphSegmenter;

  public JSI18nEngine(String lang) {
    wordSegmenter = new Segmenter(lang, JsPropertyMap.of("granularity", "word"));
    glyphSegmenter = new Segmenter(lang, JsPropertyMap.of("granularity", "grapheme"));
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

  private static class Walker implements I18nEngine.Walker {
    private final TSList<Integer> segments = new TSList<>();
    private int index;

    private Walker(Segmenter segmenter, String text) {
      JsArray<Segment> segments0 = segmenter.segment(text);
      Segment last = null;
      for (int i = 0; i < segments0.length; ++i) {
        Segment segment = segments0.getAt(i);
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
}
