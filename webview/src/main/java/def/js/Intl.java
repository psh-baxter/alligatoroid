package def.js;

@jsweet.lang.Interface
public abstract class Intl extends Object {
  @jsweet.lang.Interface
  public static class Segmenter extends Object {
    public Segmenter(java.lang.String lang, SegmenterOptions segmenterOptions) {}

    public native Segments segment(java.lang.String text);
  }

  @jsweet.lang.Interface
  public abstract static class Segments extends Iterable<Segment> {}

  public static class Segment {
    public java.lang.String segment;
    public int index;
    boolean wordLike;
  }

  public static class SegmenterOptions {
    /** grapheme, word, sentence */
    public java.lang.String granularity;
  }
}
