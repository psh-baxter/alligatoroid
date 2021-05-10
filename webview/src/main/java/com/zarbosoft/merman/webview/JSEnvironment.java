package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.webview.compat.CompatOverlay;
import com.zarbosoft.merman.webview.compat.Segmenter;
import com.zarbosoft.rendaw.common.TSList;
import elemental2.core.JsDate;
import elemental2.core.JsIIterableResult;
import elemental2.core.JsIteratorIterable;
import elemental2.core.JsObject;
import elemental2.core.Symbol;
import elemental2.dom.DataTransfer;
import elemental2.dom.DataTransferItem;
import elemental2.dom.DomGlobal;
import elemental2.promise.IThenable;
import jsinterop.base.JsPropertyMap;

import java.util.function.Consumer;

public class JSEnvironment implements Environment {
  private final Segmenter wordSegmenter;
  private final Segmenter glyphSegmenter;

  public JSEnvironment(String lang) {
    wordSegmenter = new Segmenter(lang, JsPropertyMap.of("granularity", "word"));
    glyphSegmenter = new Segmenter(lang, JsPropertyMap.of("granularity", "grapheme"));
  }

  @Override
  public Time now() {
    return new JSTime((long) new JsDate().getTime());
  }

  @Override
  public Environment.I18nWalker glyphWalker(String s) {
    return new I18nWalker(glyphSegmenter, s);
  }

  @Override
  public Environment.I18nWalker wordWalker(String s) {
    return new I18nWalker(wordSegmenter, s);
  }

  @Override
  public Environment.I18nWalker lineWalker(String s) {
    return new I18nWalker(wordSegmenter, s);
  }

  @Override
  public HandleDelay delay(long ms, Runnable r) {
    return new Handle(
        DomGlobal.setTimeout(
            new DomGlobal.SetTimeoutCallbackFn() {
              @Override
              public void onInvoke(Object... p0) {
                r.run();
              }
            },
            ms));
  }

  @Override
  public void clipboardSet(String mime, Object bytes) {
    CompatOverlay.mmCopy(mime, (String) bytes);
  }

  @Override
  public void clipboardGet(String mime, Consumer<Object> cb) {
    CompatOverlay.mmUncopy()
        .then(
            new IThenable.ThenOnFulfilledCallbackFn<DataTransfer, Object>() {
              @Override
              public IThenable<Object> onInvoke(DataTransfer d) {
                DataTransferItem backup = null;
                for (int i = 0; i < d.items.length; ++i) {
                  DataTransferItem f = d.items.getAt(i);
                  if (mime.equals(f.type)) {
                    f.getAsString(
                        new DataTransferItem.GetAsStringCallbackFn() {
                          @Override
                          public Object onInvoke(String p0) {
                            cb.accept(p0);
                            return null;
                          }
                        });
                    return null;
                  } else if (backup == null && "text/plain".equals(f.type)) {
                    backup = f;
                  }
                }
                if (backup != null) {
                  backup.getAsString(
                      s -> {
                        cb.accept(s);
                        return null;
                      });
                }
                return null;
              }
            });
  }

  @Override
  public void clipboardGetString(Consumer<String> cb) {
    CompatOverlay.mmUncopyText()
        .then(
            new IThenable.ThenOnFulfilledCallbackFn<String, Object>() {
              @Override
              public IThenable<Object> onInvoke(String p0) {
                cb.accept(p0);
                return null;
              }
            });
  }

  @Override
  public void clipboardSetString(String string) {
    CompatOverlay.mmCopyText(string);
  }

  public static class JSTime implements Environment.Time {
    public final long data;

    private JSTime(long ms) {
      this.data = ms;
    }

    @Override
    public boolean isBefore(Time other) {
      return data < ((JSTime) other).data;
    }

    @Override
    public Time plusMillis(int count) {
      return new JSTime(data + count);
    }
  }

  private static class I18nWalker implements Environment.I18nWalker {
    private final TSList<Integer> segments = new TSList<>();
    private final String text;
    private int index;

    private I18nWalker(Segmenter segmenter, String text) {
      this.text = text;
      JsObject segments0 = segmenter.segment(this.text);
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

    public int preceding(int offset) {
      while (segments.get(index) >= offset) {
        index -= 1;
        if (index < 0) {
          index = 0;
          return I18N_DONE;
        }
      }
      return segments.get(index);
    }


    public int following(int offset) {
      while (segments.get(index) <= offset) {
        index += 1;
        if (index >= segments.size()) {
          index = segments.size() - 1;
          return I18N_DONE;
        }
      }
      return segments.get(index);
    }
    @Override
    public int precedingStart(int offset) {
      int out = preceding(offset);
      if (out != -1 && Character.isWhitespace(text.codePointAt(out))) out = preceding(out);
      return out;
    }

    @Override
    public int precedingEnd(int offset) {
      int out = preceding(offset);
      if (out != -1 && !Character.isWhitespace(text.codePointAt(out))) out = preceding(out);
      if (out == -1 && offset > 0) return 0;
      return out;
    }

    @Override
    public int followingStart(int offset) {
      int out = following(offset);
      if (out != -1 && out < text.length() && Character.isWhitespace(text.codePointAt(out)))
        out = following(out);
      return out;
    }

    @Override
    public int followingEnd(int offset) {
      int out = following(offset);
      if (out != -1 && out < text.length() && !Character.isWhitespace(text.codePointAt(out)))
        out = following(out);
      return out;
    }
  }

  public static class Handle implements Environment.HandleDelay {
    final double id;

    public Handle(double id) {
      this.id = id;
    }

    @Override
    public void cancel() {
      DomGlobal.clearTimeout(id);
    }
  }
}
