package com.zarbosoft.merman.core;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class SyntaxPath {
  public final ROList<String> segments;

  public SyntaxPath(final ROList<String> segments) {
    this.segments = segments;
  }

  public SyntaxPath(final String... segments) {
    this.segments = TSList.of(segments);
  }

  public SyntaxPath add(final String section) {
    return new SyntaxPath(new TSList<>(segments).add(section));
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof SyntaxPath)) return false;
    if (((SyntaxPath) obj).segments.size() != segments.size()) return false;
    for (int i = 0; i < segments.size(); ++i) {
      if (!((SyntaxPath) obj).segments.get(i).equals(segments.get(i))) return false;
    }
    return true;
  }

  public boolean contains(final SyntaxPath other) {
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

  public SyntaxPath add(final SyntaxPath path) {
    return new SyntaxPath(new TSList<>(segments).addAll(path.segments));
  }
}
