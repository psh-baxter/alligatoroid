package com.zarbosoft.merman.editor;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class Path {
  public final ROList<String> segments;

  public Path(final ROList<String> segments) {
    this.segments = segments;
  }

  public Path(final String... segments) {
    this.segments = TSList.of(segments);
  }

  public Path add(final String section) {
    return new Path(new TSList<>(segments).add(section));
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Path)) return false;
    if (((Path) obj).segments.size() != segments.size()) return false;
    for (int i = 0; i < segments.size(); ++i) {
      if (!((Path) obj).segments.get(i).equals(segments.get(i))) return false;
    }
    return true;
  }

  public boolean contains(final Path other) {
    if (other.segments.size() > segments.size()) return false;
    for (int i = 0; i < other.segments.size(); ++i) {
      if (!other.segments.get(i).equals(segments.get(i))) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    for (String segment : segments) {
      if (out.length() > 0) {
        out.append("/");
      }
      out.append(segment);
    }
    return out.toString();
  }

  public ROList<String> toList() {
    return segments;
  }

  public Path add(final Path path) {
    return new Path(new TSList<>(segments).addAll(path.segments));
  }
}
