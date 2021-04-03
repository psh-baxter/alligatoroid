package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.I18nEngine;
import com.zarbosoft.merman.webview.compat.CompatOverlay;
import com.zarbosoft.merman.webview.compat.Segmenter;
import com.zarbosoft.rendaw.common.TSList;
import elemental2.core.JsIIterableResult;
import elemental2.core.JsIteratorIterable;
import elemental2.core.JsObject;
import elemental2.core.Symbol;
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
      JsObject segments0 = segmenter.segment(text);
      JsIteratorIterable<JsPropertyMap> iter =
          (JsIteratorIterable<JsPropertyMap>) CompatOverlay.getSymbol(segments0, Symbol.iterator);
      JsIIterableResult<JsPropertyMap> at = iter.next();
      while (!at.isDone()) {
        JsPropertyMap segment = at.getValue();
        segments.add((int) (double) segment.get("index"));
        at = iter.next();
      }
      segments.add(text.length());
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
