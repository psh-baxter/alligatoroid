package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.visual.Visual;

import static com.zarbosoft.merman.core.Environment.I18N_DONE;

public class CursorFieldPrimitive extends com.zarbosoft.merman.core.Cursor {
  public final VisualFrontPrimitive.RangeAttachment range;
  public final VisualFrontPrimitive visualPrimitive;
  private final FieldPrimitive.Listener clusterListener;
  Environment.I18nWalker clusterIterator;

  public CursorFieldPrimitive(
      final Context context,
      final VisualFrontPrimitive visualPrimitive,
      final boolean leadFirst,
      final int beginOffset,
      final int endOffset) {
    this.visualPrimitive = visualPrimitive;
    range = new VisualFrontPrimitive.RangeAttachment(visualPrimitive, true);
    range.setStyle(context, context.syntax.cursorStyle.obbox);
    range.leadFirst = leadFirst;
    range.setOffsets(context, beginOffset, endOffset);
    clusterIterator = context.env.glyphWalker(visualPrimitive.value.get());
    clusterListener = new ClusterIteratorUpdater(this, visualPrimitive);
    visualPrimitive.value.addListener(this.clusterListener);
  }

  private int precedingStart(final Environment.I18nWalker iter, final int offset) {
    int to = iter.precedingStart(offset);
    if (to == I18N_DONE) to = 0;
    return Math.max(0, to);
  }

  public int precedingStart() {
    return precedingStart(clusterIterator, range.beginOffset);
  }

  private int precedingEnd(final int offset) {
    return precedingEnd(clusterIterator, offset);
  }

  private int precedingEnd(final Environment.I18nWalker iter, final int offset) {
    int to = iter.precedingEnd(offset);
    if (to == I18N_DONE) to = 0;
    return Math.max(0, to);
  }

  public int precedingEnd() {
    return precedingEnd(clusterIterator, range.beginOffset);
  }

  public int followingStart(final int offset) {
    return followingStart(clusterIterator, offset);
  }

  private int followingStart(final Environment.I18nWalker iter, final int offset) {
    int to = iter.followingStart(offset);
    if (to == I18N_DONE) to = visualPrimitive.value.length();
    return Math.min(visualPrimitive.value.length(), to);
  }

  public int followingStart() {
    return followingStart(clusterIterator, range.endOffset);
  }

  public int followingEnd(final int offset) {
    return followingEnd(clusterIterator, offset);
  }

  private int followingEnd(final Environment.I18nWalker iter, final int offset) {
    int to = iter.followingEnd(offset);
    if (to == I18N_DONE) to = visualPrimitive.value.length();
    return Math.min(visualPrimitive.value.length(), to);
  }

  public int followingEnd() {
    return followingEnd(clusterIterator, range.endOffset);
  }

  private int nextWordStart(Context context, final int source) {
    final Environment.I18nWalker iter = context.env.wordWalker(visualPrimitive.value.get());
    return followingStart(iter, source);
  }

  private int nextWordEnd(Context context, final int source) {
    final Environment.I18nWalker iter = context.env.wordWalker(visualPrimitive.value.get());
    return followingEnd(iter, source);
  }

  private int previousWordStart(Context context, final int source) {
    final Environment.I18nWalker iter = context.env.wordWalker(visualPrimitive.value.get());
    return precedingStart(iter, source);
  }

  private int nextLine(final VisualFrontPrimitive.Line sourceLine, final int source) {
    if (sourceLine.index + 1 < visualPrimitive.lines.size()) {
      final VisualFrontPrimitive.Line nextLine = visualPrimitive.lines.get(sourceLine.index + 1);
      return nextLine.offset + Math.min(nextLine.text.length(), source - sourceLine.offset);
    } else return sourceLine.offset + sourceLine.text.length();
  }

  private int previousLine(final VisualFrontPrimitive.Line sourceLine, final int source) {
    if (sourceLine.index > 0) {
      final VisualFrontPrimitive.Line previousLine =
          visualPrimitive.lines.get(sourceLine.index - 1);
      return previousLine.offset + Math.min(previousLine.text.length(), source - sourceLine.offset);
    } else return sourceLine.offset;
  }

  private int endOfLine(final VisualFrontPrimitive.Line sourceLine) {
    return sourceLine.offset + sourceLine.text.length();
  }

  private int startOfLine(final VisualFrontPrimitive.Line sourceLine) {
    return sourceLine.offset;
  }

  @Override
  public void destroy(final Context context) {
    range.destroy(context);
    visualPrimitive.cursor = null;
    visualPrimitive.commit();
    visualPrimitive.value.removeListener(clusterListener);
  }

  @Override
  public Visual getVisual() {
    return visualPrimitive;
  }

  @Override
  public CursorState saveState() {
    return new VisualFrontPrimitive.PrimitiveCursorState(
        visualPrimitive.value, range.leadFirst, range.beginOffset, range.endOffset);
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visualPrimitive.value.getSyntaxPath().add(String.valueOf(range.leadIndex()));
  }

  public void actionNextGlyph(final Context context) {
    final int newIndex = followingStart();
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionPreviousGlyph(final Context context) {
    final int newIndex = precedingStart();
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionNextWord(final Context context) {
    final int newIndex = nextWordStart(context, range.endOffset);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionGatherFirst(Context context) {
    if (range.beginOffset == 0) return;
    range.setBeginOffset(context, 0);
  }

  public void actionGatherLast(Context context) {
    int last = visualPrimitive.value.length();
    if (range.endOffset == last) return;
    range.setEndOffset(context, last);
  }

  public void actionFirstGlyph(Context context) {
    if (range.beginOffset == 0 && range.endOffset == 0) return;
    range.setOffsets(context, 0);
  }

  public void actionLastGlyph(Context context) {
    if (range.beginOffset == 0 && range.endOffset == 0) return;
    range.setOffsets(context, visualPrimitive.value.length());
  }

  public void actionPreviousWord(final Context context) {
    final int newIndex = previousWordStart(context, range.beginOffset);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionLineBegin(final Context context) {
    final int newIndex = startOfLine(range.beginLine);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionLineEnd(final Context context) {
    final int newIndex = endOfLine(range.endLine);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionNextLine(final Context context) {
    final int newIndex = nextLine(range.endLine, range.endOffset);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionPreviousLine(final Context context) {
    final int newIndex = previousLine(range.beginLine, range.beginOffset);
    if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
    range.setOffsets(context, newIndex);
  }

  public void actionCopy(final Context context) {
    visualPrimitive.copy(context, range.beginOffset, range.endOffset);
  }

  public void actionGatherNextGlyph(final Context context) {
    final int newIndex = followingStart();
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionGatherNextWord(final Context context) {
    final int newIndex = nextWordEnd(context, range.endOffset);
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionGatherNextLineEnd(final Context context) {
    final int newIndex = endOfLine(range.endLine);
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionGatherNextLine(final Context context) {
    final int newIndex = nextLine(range.endLine, range.endOffset);
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionReleaseNextGlyph(final Context context) {
    final int newIndex = Math.max(range.beginOffset, precedingEnd(range.endOffset));
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionReleaseNextWord(final Context context) {
    final int newIndex = Math.max(range.beginOffset, previousWordStart(context, range.endOffset));
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionReleaseNextLineEnd(final Context context) {
    final int newIndex = Math.max(range.beginOffset, startOfLine(range.endLine));
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionReleaseNextLine(final Context context) {
    final int newIndex = Math.max(range.beginOffset, previousLine(range.endLine, range.endOffset));
    if (range.endOffset == newIndex) return;
    range.setEndOffset(context, newIndex);
  }

  public void actionReleaseAll(final Context context) {
    if (range.beginOffset == range.endOffset) return;
    range.setOffsets(context, range.leadFirst ? range.beginOffset : range.endOffset);
  }

  public void actionGatherPreviousGlyph(final Context context) {
    final int newIndex = precedingStart();
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionGatherPreviousWord(final Context context) {
    final int newIndex = previousWordStart(context, range.beginOffset);
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionGatherPreviousLineStart(final Context context) {
    final int newIndex = startOfLine(range.beginLine);
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionGatherPreviousLine(final Context context) {
    final int newIndex = previousLine(range.beginLine, range.beginOffset);
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionReleasePreviousGlyph(final Context context) {
    final int newIndex = Math.min(range.endOffset, followingStart(range.beginOffset));
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionReleasePreviousWord(final Context context) {
    final int newIndex = Math.min(range.endOffset, nextWordStart(context, range.beginOffset));
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionReleasePreviousLineStart(final Context context) {
    final int newIndex = Math.min(range.endOffset, endOfLine(range.beginLine));
    if (range.beginOffset == newIndex) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionReleasePreviousLine(final Context context) {
    final int newIndex = Math.min(range.endOffset, nextLine(range.beginLine, range.beginOffset));
    if (newIndex == range.beginOffset) return;
    range.setBeginOffset(context, newIndex);
  }

  public void actionExit(final Context context) {
    if (visualPrimitive.value.atomParentRef == null) return;
    visualPrimitive.value.atomParentRef.selectParent(context);
  }

  private static class ClusterIteratorUpdater implements FieldPrimitive.Listener {
    private final VisualFrontPrimitive visualPrimitive;
    private final CursorFieldPrimitive primitiveCursor;

    public ClusterIteratorUpdater(
        CursorFieldPrimitive primitiveCursor, VisualFrontPrimitive visualPrimitive) {
      this.visualPrimitive = visualPrimitive;
      this.primitiveCursor = primitiveCursor;
    }

    @Override
    public void changed(Context context, int index, int remove, String add) {
      primitiveCursor.clusterIterator = context.env.glyphWalker(visualPrimitive.value.get());
    }
  }
}
