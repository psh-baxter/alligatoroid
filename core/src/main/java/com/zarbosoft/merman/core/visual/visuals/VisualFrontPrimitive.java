package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.SelectionState;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.attachments.CursorAttachment;
import com.zarbosoft.merman.core.visual.attachments.TextBorderAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.merman.core.wall.bricks.BrickLine;
import com.zarbosoft.merman.core.wall.bricks.BrickText;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.function.Function;

import static com.zarbosoft.merman.core.Environment.I18N_DONE;
import static com.zarbosoft.merman.core.syntax.style.Style.SplitMode.ALWAYS;

public class VisualFrontPrimitive extends Visual implements VisualLeaf {
  public final FieldPrimitive value;
  // INVARIANT: Leaf nodes must always create at least one brick
  // INVARIANT: Always at least one line
  // TODO index line offsets for faster insert/remove
  private final FieldPrimitive.Listener dataListener;
  private final FrontPrimitiveSpec spec;
  public VisualParent parent;
  public int brickCount = 0;
  public PrimitiveHoverable hoverable;
  public Cursor selection;
  public TSList<Line> lines = new TSList<>();
  public int hardLineCount = 0;

  public VisualFrontPrimitive(
      final Context context,
      final VisualParent parent,
      FrontPrimitiveSpec frontPrimitiveSpec,
      final FieldPrimitive value,
      final int visualDepth) {
    super(visualDepth);
    this.parent = parent;
    this.value = value;
    this.spec = frontPrimitiveSpec;
    value.visual = this;
    dataListener = new DataListener(this);
    value.addListener(dataListener);
    set(context, value.get());
  }

  private int findContaining(final int offset) {
    for (Line line : lines) {
      if (line.offset + line.text.length() < offset) continue;
      return line.index;
    }
    return lines.size();
  }

  private void idleLayBricks(final Context context, final int start, final int end) {
    final Function<Integer, Brick> accessor = i -> lines.get(i).brick;
    context.triggerIdleLayBricks(parent, start, end - start, lines.size(), accessor, accessor);
  }

  public boolean softWrapped() {
    return lines.size() > hardLineCount;
  }

  private void set(final Context context, final String text) {
    clear(context);
    int offset = 0;
    hardLineCount = 0;
    String[] rawLines = text.split("\n", -1);
    for (int i = 0; i < rawLines.length; ++i) {
      String rawLine = rawLines[i];
      final Line line = new Line(this, true);
      hardLineCount += 1;
      line.setText(context, rawLine);
      line.setIndex(context, i);
      line.offset = offset;
      this.lines.add(line);
      offset += 1 + rawLine.length();
    }
    if (selection != null) {
      selection.range.setOffsets(
          context, Math.max(0, Math.min(text.length(), selection.range.beginOffset)));
    }
  }

  private void clear(final Context context) {
    for (final Line line : lines) {
      line.destroy(context);
    }
    lines.clear();
    hardLineCount = 0;
  }

  private void renumber(int index, int offset) {
    for (; index < lines.size(); ++index) {
      final Line line = lines.get(index);
      if (line.hard) offset += 1;
      line.index = index;
      line.offset = offset;
      offset += line.text.length();
    }
  }

  public void select(
      final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
    if (selection != null) {
      selection.range.leadFirst = leadFirst;
      selection.range.setOffsets(context, beginOffset, endOffset);
    } else {
      selection = createSelection(context, leadFirst, beginOffset, endOffset);
      context.setCursor(selection);
    }
  }

  public Cursor createSelection(
      final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
    return context.cursorFactory.createPrimitiveCursor(
        context, this, leadFirst, beginOffset, endOffset);
  }

  protected void commit() {}

  @Override
  public VisualParent parent() {
    return parent;
  }

  @Override
  public Brick createOrGetCornerstoneCandidate(final Context context) {
    return lines.get(0).createOrGetBrick(context);
  }

  @Override
  public Brick createFirstBrick(final Context context) {
    return lines.get(0).createBrick(context);
  }

  @Override
  public Brick createLastBrick(final Context context) {
    return lines.last().createBrick(context);
  }

  @Override
  public Brick getFirstBrick(final Context context) {
    return lines.get(0).brick;
  }

  @Override
  public Brick getLastBrick(final Context context) {
    return lines.last().brick;
  }

  /**
   * Fine grained compact/expand, done last during compact/first during expand
   *
   * @param context
   */
  public void primitiveReflow(Context context) {
    final ResplitResult result = new ResplitResult();
    boolean anyOver = false;
    boolean allUnder = true;
    for (int i = lines.size() - 1; i >= 0; --i) {
      final Line line = lines.get(i);
      if (line.brick == null) continue;
      final double edge = line.brick.converseEdge();
      if (!anyOver && edge > context.edge) {
        anyOver = true;
      }
      if (edge <= context.edge && edge * context.retryExpandFactor >= context.edge)
        allUnder = false;
      if (line.hard && (anyOver || allUnder)) {
        result.merge(resplitOne(context, i));
        anyOver = false;
        allUnder = true;
      }
    }
  }

  @Override
  public void compact(final Context context) {
    lines.get(0).brick.changed(context);
  }

  @Override
  public void expand(final Context context) {
    lines.get(0).brick.changed(context);
  }

  @Override
  public void getLeafBricks(final Context context, TSList<Brick> bricks) {
    for (Line line : lines) {
      if (line.brick == null) continue;
      bricks.add(line.brick);
    }
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (selection != null) context.clearCursor();
    if (hoverable != null) context.clearHover();
    value.removeListener(dataListener);
    value.visual = null;
    clear(context);
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    super.root(context, parent, visualDepth, depthScore);
    // Force expand
    final StringBuilder aggregate = new StringBuilder();
    for (int i = lines.size() - 1; i >= 0; --i) {
      final Line line = lines.get(i);
      aggregate.insert(0, line.text);
      if (line.hard) {
        line.setText(context, aggregate.toString());
        aggregate.setLength(0);
      }
    }
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    value.selectInto(context);
    return true;
  }

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    if (parent == null) return null;
    return parent.hover(context, point);
  }

  private ResplitResult resplitOne(final Context context, final int i) {
    final VisualAtom atom = parent.atomVisual();
    final ResplitResult result = new ResplitResult();
    final ResplitOneBuilder build = new ResplitOneBuilder(context);
    final int modifiedOffsetStart = lines.get(i).offset;
    build.offset = modifiedOffsetStart;

    int endIndex = i;
    final int modifiedLength;

    // Get the full unwrapped text
    {
      final StringBuilder sum = new StringBuilder();
      for (int j = i; j < lines.size(); ++j, ++endIndex) {
        final Line line = lines.get(j);
        if (j > i && line.hard) break;
        sum.append(line.text);
      }
      build.text = sum.toString();
      modifiedLength = build.text.length();
    }

    int j = i;

    // Wrap text into existing lines
    for (; j < endIndex; ++j) {
      final Line line = lines.get(j);
      if (!build.hasText() && j > i) break;
      final Font font;
      final double converse;
      if (line.brick == null) {
        final Style style = j == 0 ? spec.firstStyle : j == i ? spec.hardStyle : spec.softStyle;
        font = Context.getFont(context, style);
        final Alignment alignment = atom.findAlignment(style.alignment);
        if (alignment == null) converse = 0;
        else converse = alignment.converse;
      } else {
        font = line.brick.getFont();
        converse = line.brick.getConverse();
      }
      result.merge(build.build(line, font, converse));
    }

    // If text remains, make new lines
    Integer firstLineCreated = null;
    Integer lastLineCreated = null;
    if (build.hasText()) {
      firstLineCreated = j;
      while (build.hasText()) {
        final Line line = new Line(this, false);
        line.setIndex(context, j);
        final Style style = spec.softStyle;
        final Font font = Context.getFont(context, style);
        final Alignment alignment = atom.findAlignment(style.alignment);
        final double converse;
        if (alignment == null) converse = 0;
        else converse = alignment.converse;
        build.build(line, font, converse);
        lines.insert(j, line);
        ++j;
      }
      lastLineCreated = j;
    }

    // If ran out of text early, delete following soft lines
    if (j < endIndex) {
      result.changed = true;
      final ROList<Line> oldLines = lines.sublist(j, endIndex).mut();
      lines.sublist(j, endIndex).clear();
      for (final Line line : oldLines) {
        if (line.hard) hardLineCount -= 1;
        line.destroy(context);
      }
    }

    // Cleanup
    renumber(j, build.offset);

    if (firstLineCreated != null) {
      idleLayBricks(context, firstLineCreated, lastLineCreated - firstLineCreated);
    }

    // Adjust hover/selection
    if (hoverable != null) {
      if (hoverable.range.beginOffset >= modifiedOffsetStart + modifiedLength) {
        hoverable.range.nudge(context);
      } else if (hoverable.range.beginOffset >= modifiedOffsetStart
          || hoverable.range.endOffset >= modifiedOffsetStart) {
        context.clearHover();
      }
    }
    if (selection != null) {
      selection.range.nudge(context);
    }

    return result;
  }

  private static class ResplitOneBuilder {
    private final Context context;
    String text;
    int offset;

    public ResplitOneBuilder(Context context) {
      this.context = context;
    }

    public boolean hasText() {
      return !text.isEmpty();
    }

    public ResplitResult build(final Line line, final Font font, final double converse) {
      final ResplitResult result = new ResplitResult();
      Font.Measurer measurer = font.measurer();
      final double width = measurer.getWidth(text);
      final double edge = converse + width;
      int split;
      if (converse < context.edge && edge > context.edge) {
        final Environment.I18nWalker lineIter = context.env.lineWalker(text);
        final double edgeOffset = context.edge - converse;
        final int under = measurer.getIndexAtConverse(context, text, edgeOffset);
        if (under == text.length()) split = under;
        else {
          split = lineIter.precedingStart(under + 1);
          if (split == 0 || split == I18N_DONE) {
            final Environment.I18nWalker clusterIter = context.env.glyphWalker(text);
            split = clusterIter.precedingStart(under + 1);
          }
          if (split < 4 || split == I18N_DONE) {
            split = text.length();
            result.compactLimit = true;
          }
        }
      } else {
        split = text.length();
      }

      final String newText = text.substring(0, split);
      if (!newText.equals(line.text)) line.setText(context, newText);
      if (line.offset == offset) result.changed = false;
      else result.changed = true;
      line.offset = offset;
      text = text.substring(split);
      offset += split;
      return result;
    }
  }

  public abstract static class BoundsListener {
    public abstract void firstChanged(Context context, Brick brick);

    public abstract void lastChanged(Context context, Brick brick);
  }

  public static class PrimitiveSelectionState implements SelectionState {
    private final FieldPrimitive value;
    private final int beginOffset;
    private final int endOffset;
    private final boolean leadFirst;

    public PrimitiveSelectionState(
        final FieldPrimitive value,
        final boolean leadFirst,
        final int beginOffset,
        final int endOffset) {
      this.value = value;
      this.leadFirst = leadFirst;
      this.beginOffset = beginOffset;
      this.endOffset = endOffset;
    }

    @Override
    public void select(final Context context) {
      ((VisualFrontPrimitive) value.visual).select(context, leadFirst, beginOffset, endOffset);
    }
  }

  private static class ResplitResult {
    boolean changed = false;
    boolean compactLimit = false;

    public void merge(final ResplitResult other) {
      changed = changed || other.changed;
      compactLimit = compactLimit || other.compactLimit;
    }
  }

  public static class Cursor extends com.zarbosoft.merman.core.Cursor {
    public final RangeAttachment range;
    public final VisualFrontPrimitive visualPrimitive;
    private final FieldPrimitive.Listener clusterListener;
    Environment.I18nWalker clusterIterator;

    public Cursor(
        final Context context,
        final VisualFrontPrimitive visualPrimitive,
        final boolean leadFirst,
        final int beginOffset,
        final int endOffset) {
      this.visualPrimitive = visualPrimitive;
      range = new RangeAttachment(visualPrimitive, true);
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

    private int nextLine(final Line sourceLine, final int source) {
      if (sourceLine.index + 1 < visualPrimitive.lines.size()) {
        final Line nextLine = visualPrimitive.lines.get(sourceLine.index + 1);
        return nextLine.offset + Math.min(nextLine.text.length(), source - sourceLine.offset);
      } else return sourceLine.offset + sourceLine.text.length();
    }

    private int previousLine(final Line sourceLine, final int source) {
      if (sourceLine.index > 0) {
        final Line previousLine = visualPrimitive.lines.get(sourceLine.index - 1);
        return previousLine.offset
            + Math.min(previousLine.text.length(), source - sourceLine.offset);
      } else return sourceLine.offset;
    }

    private int endOfLine(final Line sourceLine) {
      return sourceLine.offset + sourceLine.text.length();
    }

    private int startOfLine(final Line sourceLine) {
      return sourceLine.offset;
    }

    @Override
    public void destroy(final Context context) {
      range.destroy(context);
      visualPrimitive.selection = null;
      visualPrimitive.commit();
      visualPrimitive.value.removeListener(clusterListener);
    }

    @Override
    public Visual getVisual() {
      return visualPrimitive;
    }

    @Override
    public SelectionState saveState() {
      return new PrimitiveSelectionState(
          visualPrimitive.value, range.leadFirst, range.beginOffset, range.endOffset);
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return visualPrimitive.value.getSyntaxPath().add(String.valueOf(range.leadIndex()));
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    public void actionPrevious(final Context context) {
      visualPrimitive.parent.selectPrevious(context);
    }

    public void actionNextElement(final Context context) {
      final int newIndex = followingStart();
      if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
      range.setOffsets(context, newIndex);
    }

    public void actionPreviousElement(final Context context) {
      final int newIndex = precedingStart();
      if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
      range.setOffsets(context, newIndex);
    }

    public void actionNextWord(final Context context) {
      final int newIndex = nextWordStart(context, range.endOffset);
      if (range.beginOffset == newIndex && range.endOffset == newIndex) return;
      range.setOffsets(context, newIndex);
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
      context.copy(visualPrimitive.value.get().substring(range.beginOffset, range.endOffset));
    }

    public void actionGatherNext(final Context context) {
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

    public void actionReleaseNext(final Context context) {
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
      final int newIndex =
          Math.max(range.beginOffset, previousLine(range.endLine, range.endOffset));
      if (range.endOffset == newIndex) return;
      range.setEndOffset(context, newIndex);
    }

    public void actionGatherPrevious(final Context context) {
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

    public void actionReleasePrevious(final Context context) {
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

    public void actionNext(final Context context) {
      visualPrimitive.parent.selectNext(context);
    }

    public void actionExit(final Context context) {
      if (visualPrimitive.value.atomParentRef == null) return;
      visualPrimitive.value.atomParentRef.selectAtomParent(context);
    }

    private static class ClusterIteratorUpdater implements FieldPrimitive.Listener {
      private final VisualFrontPrimitive visualPrimitive;
      private final Cursor primitiveCursor;

      public ClusterIteratorUpdater(Cursor primitiveCursor, VisualFrontPrimitive visualPrimitive) {
        this.visualPrimitive = visualPrimitive;
        this.primitiveCursor = primitiveCursor;
      }

      @Override
      public void changed(Context context, int index, int remove, String add) {
        primitiveCursor.clusterIterator = context.env.glyphWalker(visualPrimitive.value.get());
      }
    }
  }

  private static class DataListener implements FieldPrimitive.Listener {
    private final VisualFrontPrimitive visualFrontPrimitive;

    public DataListener(VisualFrontPrimitive visualFrontPrimitive) {
      this.visualFrontPrimitive = visualFrontPrimitive;
    }

    @Override
    public void changed(Context context, int index, int remove, String add) {
      removed(context, index, remove);
      added(context, index, add);
    }

    public void added(final Context context, final int offset, final String text) {
      final TSList<String> segments = TSList.of(text.split("\n", -1));
      if (segments.isEmpty()) return;
      segments.reverse();
      final int originalIndex = visualFrontPrimitive.findContaining(offset);
      int index = originalIndex;
      Line line = visualFrontPrimitive.lines.get(index);

      int movingOffset = offset;

      // Insert text into first line at offset
      final StringBuilder builder = new StringBuilder(line.text);
      String segment = segments.removeLast();
      builder.insert(movingOffset - line.offset, segment);
      String remainder = null;
      if (!segments.isEmpty()) {
        remainder = builder.substring(movingOffset - line.offset + segment.length());
        builder.delete(movingOffset - line.offset + segment.length(), builder.length());
      }
      line.setText(context, builder.toString());
      movingOffset = line.offset;

      // Add new hard lines for remaining segments
      final int firstLineCreated = index + 1;
      while (true) {
        index += 1;
        movingOffset += line.text.length();
        segment = segments.removeLastOpt();
        if (segment == null) break;
        line = new Line(visualFrontPrimitive, true);
        visualFrontPrimitive.hardLineCount += 1;
        line.setText(context, segment);
        line.setIndex(context, index);
        movingOffset += 1;
        line.offset = movingOffset;
        visualFrontPrimitive.lines.insert(index, line);
      }
      final int lastLineCreated = index + 1;
      if (remainder != null) line.setText(context, line.text + remainder);

      // Renumber/adjust offset of following lines
      visualFrontPrimitive.renumber(index, movingOffset);

      if (visualFrontPrimitive.selection != null) {
        final int newBegin;
        if (visualFrontPrimitive.selection.range.beginOffset < offset)
          newBegin = visualFrontPrimitive.selection.range.beginOffset;
        else newBegin = visualFrontPrimitive.selection.range.beginOffset + text.length();
        visualFrontPrimitive.selection.range.setOffsets(context, newBegin);
      }

      visualFrontPrimitive.idleLayBricks(
          context, firstLineCreated, lastLineCreated - firstLineCreated);
    }

    public void removed(final Context context, final int offset, final int count) {
      int remaining = count;
      final Line base = visualFrontPrimitive.lines.get(visualFrontPrimitive.findContaining(offset));

      // Remove text from first line
      {
        final int exciseStart = offset - base.offset;
        final int exciseEnd = Math.min(exciseStart + remaining, base.text.length());
        final String newText = base.text.substring(0, exciseStart) + base.text.substring(exciseEnd);
        base.setText(context, newText);
        remaining -= exciseEnd - exciseStart;
      }

      // Remove text from subsequent lines
      int index = base.index + 1;
      int removeLines = 0;
      while (remaining > 0) {
        final Line line = visualFrontPrimitive.lines.get(index++);
        if (line.hard) {
          remaining -= 1;
        }
        final int exciseEnd = Math.min(remaining, line.text.length());
        base.setText(context, base.text + line.text.substring(exciseEnd));
        remaining -= exciseEnd;
        if (line.hard) visualFrontPrimitive.hardLineCount -= 1;
        removeLines += 1;
      }
      final TSList<Line> sublist =
          visualFrontPrimitive.lines.sublist(base.index + 1, base.index + 1 + removeLines);
      final ROList<Line> oldSublist = sublist.mut();
      sublist.clear();
      for (final Line line : oldSublist) line.destroy(context);
      for (int i = base.index + 1; i < visualFrontPrimitive.lines.size(); ++i) {
        Line line = visualFrontPrimitive.lines.get(i);
        line.index = base.index + 1 + i;
        line.offset -= count;
      }
      if (visualFrontPrimitive.hoverable != null) {
        if (visualFrontPrimitive.hoverable.range.beginOffset >= offset + count) {
          visualFrontPrimitive.hoverable.range.setOffsets(
              context, visualFrontPrimitive.hoverable.range.beginOffset - (offset + count));
        } else if (visualFrontPrimitive.hoverable.range.beginOffset >= offset
            || visualFrontPrimitive.hoverable.range.endOffset >= offset) {
          context.clearHover();
        }
      }
      if (visualFrontPrimitive.selection != null) {
        int newBegin = visualFrontPrimitive.selection.range.beginOffset;
        int newEnd = visualFrontPrimitive.selection.range.endOffset;
        if (newBegin >= offset + count) newBegin = newBegin - count;
        else if (newBegin >= offset) newBegin = offset;
        if (newEnd >= offset + count) newEnd = newEnd - count;
        else if (newEnd >= offset) newEnd = offset;
        visualFrontPrimitive.selection.range.setOffsets(context, newBegin, newEnd);
      }
    }
  }

  public static class RangeAttachment {
    private final boolean forSelection;
    private final VisualFrontPrimitive visualFrontPrimitive;
    public CursorAttachment cursor;
    public int beginOffset;
    public int endOffset;
    public Line beginLine;
    public Line endLine;
    boolean leadFirst;
    TextBorderAttachment border;
    TSSet<BoundsListener> listeners = new TSSet<>();
    private ObboxStyle style;

    private RangeAttachment(VisualFrontPrimitive visualFrontPrimitive, final boolean forSelection) {
      this.forSelection = forSelection;
      this.visualFrontPrimitive = visualFrontPrimitive;
    }

    private void setOffsets(final Context context, final int beginOffset, final int endOffset) {
      setOffsetsInternal(context, beginOffset, endOffset);
    }

    private void setOffsetsInternal(
        final Context context, final int beginOffset, final int endOffset) {
      final boolean wasPoint = this.beginOffset == this.endOffset;
      this.beginOffset = Math.max(0, Math.min(visualFrontPrimitive.value.length(), beginOffset));
      this.endOffset =
          Math.max(beginOffset, Math.min(visualFrontPrimitive.value.length(), endOffset));
      if (beginOffset == endOffset) {
        if (border != null) {
          border.destroy(context);
          border = null;
        }
        if (cursor == null) {
          cursor = new CursorAttachment(context);
          cursor.setStyle(context, style);
        }
        final int index = visualFrontPrimitive.findContaining(beginOffset);
        beginLine = endLine = visualFrontPrimitive.lines.get(index);
        setCornerstone(context, index);
        cursor.setPosition(context, beginLine.brick, beginOffset - beginLine.offset);
        for (BoundsListener l : listeners.copy()) {
          l.firstChanged(context, beginLine.brick);
          l.lastChanged(context, beginLine.brick);
        }
      } else {
        if (wasPoint) {
          beginLine = null;
          endLine = null;
        }
        if (cursor != null) {
          cursor.destroy(context);
          cursor = null;
        }
        final BrickText newFirstBrick;
        final BrickText newLastBrick;
        final int beginIndex = visualFrontPrimitive.findContaining(beginOffset);
        if (beginLine != null && beginLine.index == beginIndex) {
          newFirstBrick = beginLine.brick;
        } else {
          beginLine = visualFrontPrimitive.lines.get(beginIndex);
          if (beginLine.brick != null) newFirstBrick = beginLine.brick;
          else newFirstBrick = null;
        }
        int newFirstIndex = beginOffset - beginLine.offset;
        final int endIndex = visualFrontPrimitive.findContaining(endOffset);
        if (endLine != null && endLine.index == endIndex) {
          newLastBrick = endLine.brick;
        } else {
          endLine = visualFrontPrimitive.lines.get(endIndex);
          if (endLine.brick != null) newLastBrick = endLine.brick;
          else newLastBrick = null;
        }
        int newLastIndex = endOffset - endLine.offset;
        if (border == null) {
          border = new TextBorderAttachment(context);
          border.setStyle(context, style);
        }
        if (leadFirst) {
          if (newFirstBrick != null) setCornerstone(context, beginIndex);
        } else {
          if (newLastBrick != null) setCornerstone(context, endIndex);
        }
        border.setBoth(context, newFirstBrick, newFirstIndex, newLastBrick, newLastIndex);
        if (newFirstBrick != null)
          for (BoundsListener l : listeners) {
            l.firstChanged(context, beginLine.brick);
          }
        if (newFirstBrick != null)
          for (BoundsListener l : listeners) {
            l.lastChanged(context, beginLine.brick);
          }
      }
    }

    private void setCornerstone(final Context context, final int index) {
      if (!forSelection) return;
      context.wall.setCornerstone(
          context,
          visualFrontPrimitive.lines.get(index).createOrGetBrick(context),
          () -> {
            for (int at = index - 1; at >= 0; --at) {
              final Brick found = visualFrontPrimitive.lines.get(at).brick;
              if (found != null) return found;
            }
            return visualFrontPrimitive.parent.findPreviousBrick(context);
          },
          () -> {
            for (int at = index + 1; at < visualFrontPrimitive.lines.size(); ++at) {
              final Brick found = visualFrontPrimitive.lines.get(at).brick;
              if (found != null) return found;
            }
            return visualFrontPrimitive.parent.findNextBrick(context);
          });
    }

    private void setOffsets(final Context context, final int offset) {
      setOffsetsInternal(context, offset, offset);
    }

    private void setBeginOffset(final Context context, final int offset) {
      leadFirst = true;
      setOffsetsInternal(context, offset, endOffset);
    }

    private void setEndOffset(final Context context, final int offset) {
      leadFirst = false;
      setOffsetsInternal(context, beginOffset, offset);
    }

    public void destroy(final Context context) {
      if (border != null) border.destroy(context);
      if (cursor != null) cursor.destroy(context);
    }

    public void nudge(final Context context) {
      setOffsetsInternal(context, beginOffset, endOffset);
    }

    public void addListener(final Context context, final BoundsListener listener) {
      listeners.add(listener);
      if (beginLine != null && beginLine.brick != null)
        listener.firstChanged(context, beginLine.brick);
      if (endLine != null && endLine.brick != null) listener.lastChanged(context, endLine.brick);
    }

    public void removeListener(final BoundsListener listener) {
      listeners.remove(listener);
    }

    public void setStyle(final Context context, final ObboxStyle style) {
      this.style = style;
      if (border != null) border.setStyle(context, style);
      if (cursor != null) cursor.setStyle(context, style);
    }

    public int leadIndex() {
      return leadFirst ? beginOffset : endOffset;
    }
  }

  public static class PrimitiveHoverable extends Hoverable {
    private final VisualFrontPrimitive visual;
    RangeAttachment range;

    PrimitiveHoverable(VisualFrontPrimitive visual, final Context context) {
      range = new RangeAttachment(visual, false);
      range.setStyle(context, context.syntax.hoverStyle.obbox);
      this.visual = visual;
    }

    public void setPosition(final Context context, final int offset) {
      range.setOffsets(context, offset);
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return visual.value.getSyntaxPath().add(String.valueOf(range.leadIndex()));
    }

    @Override
    public void clear(final Context context) {
      range.destroy(context);
      visual.hoverable = null;
    }

    @Override
    public void select(final Context context) {
      visual.select(context, true, range.beginOffset, range.endOffset);
    }

    @Override
    public VisualAtom atom() {
      return visual.parent.atomVisual();
    }

    @Override
    public Visual visual() {
      return visual;
    }
  }

  public static class Line implements BrickInterface {
    public final boolean hard;
    private final VisualFrontPrimitive visual;
    public int offset;
    public String text;
    public BrickLine brick;
    public int index;

    private Line(VisualFrontPrimitive visual, final boolean hard) {
      this.hard = hard;
      this.visual = visual;
    }

    public void destroy(final Context context) {
      if (brick != null) {
        brick.destroy(context);
      }
    }

    public void setText(final Context context, final String text) {
      this.text = text;
      if (brick != null) brick.setText(context, text);
    }

    public void setIndex(final Context context, final int index) {
      if (this.index == 0 && brick != null) brick.changed(context);
      this.index = index;
    }

    public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
      if (visual.selection == null) {
        final ROPair<Hoverable, Boolean> out = visual.hover(context, point);
        if (out != null) return out;
      }
      boolean changed = false;
      if (visual.hoverable == null) {
        visual.hoverable = new VisualFrontPrimitive.PrimitiveHoverable(visual, context);
        changed = true;
      }
      int newIndex = offset + brick.getUnder(context, point);
      if (visual.hoverable.range.leadIndex() != newIndex) changed = true;
      visual.hoverable.setPosition(context, newIndex);
      return new ROPair<>(visual.hoverable, changed);
    }

    @Override
    public VisualLeaf getVisual() {
      return visual;
    }

    @Override
    public Brick createPrevious(final Context context) {
      return createPreviousBrick(context);
    }

    public Brick createPreviousBrick(final Context context) {
      if (index == 0) return visual.parent.createPreviousBrick(context);
      return visual.lines.get(index - 1).createBrick(context);
    }

    public Brick createBrick(final Context context) {
      if (brick != null) return null;
      createBrickInternal(context);
      if (visual.selection != null
          && (visual.selection.range.beginLine == Line.this
              || visual.selection.range.endLine == Line.this))
        visual.selection.range.nudge(context);
      return brick;
    }

    public Brick createBrickInternal(final Context context) {
      brick =
          new BrickLine(
              context,
              this,
              index == 0 ? visual.spec.splitMode : ALWAYS,
              index == 0
                  ? visual.spec.firstStyle
                  : hard ? visual.spec.hardStyle : visual.spec.softStyle);
      brick.setText(context, text);
      return brick;
    }

    @Override
    public Brick createNext(final Context context) {
      return createNextBrick(context);
    }

    public Brick createNextBrick(final Context context) {
      if (index == visual.lines.size() - 1) {
        return visual.parent.createNextBrick(context);
      }
      return visual.lines.get(index + 1).createBrick(context);
    }

    @Override
    public void brickDestroyed(final Context context) {
      brick = null;
    }

    @Override
    public Alignment findAlignment(String alignment) {
      return visual.parent.atomVisual().findAlignment(alignment);
    }

    public Brick createOrGetBrick(final Context context) {
      if (brick != null) return brick;
      return createBrickInternal(context);
    }
  }
}
