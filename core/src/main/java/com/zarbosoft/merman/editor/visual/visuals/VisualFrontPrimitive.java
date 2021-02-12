package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.SelectionState;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.merman.editor.visual.attachments.TextBorderAttachment;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickLine;
import com.zarbosoft.merman.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.style.ObboxStyle;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.function.Function;

public class VisualFrontPrimitive extends Visual implements VisualLeaf {
  public final ValuePrimitive value;
  // INVARIANT: Leaf nodes must always create at least one brick
  // INVARIANT: Always at least one line
  // TODO index line offsets for faster insert/remove
  private final ValuePrimitive.Listener dataListener;
  private final BrickStyle brickStyle;
  private final FrontPrimitiveSpec spec;
  public VisualParent parent;
  public int brickCount = 0;
  public PrimitiveHoverable hoverable;
  public PrimitiveCursor selection;
  public TSList<Line> lines = new TSList<>();
  private boolean canExpand = false;
  private int hardLineCount = 0;
  private IterationResplit idleResplit = null;

  public VisualFrontPrimitive(
      final Context context,
      final VisualParent parent,
      FrontPrimitiveSpec frontPrimitiveSpec,
      final ValuePrimitive value,
      final int visualDepth) {
    super(visualDepth);
    this.parent = parent;
    this.value = value;
    this.spec = frontPrimitiveSpec;
    brickStyle = new BrickStyle(context);
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
    if (canExpand) context.foreground.splitPrimitives.remove(this);
    for (final Line line : lines) {
      line.destroy(context);
    }
    lines.clear();
    hardLineCount = 0;
    canExpand = false;
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

  public TSSet<String> softTags(Context context) {
    return baseTags(context).add(Tags.TAG_PRIMITIVE_SOFT);
  }

  public TSSet<String> firstTags(Context context) {
    return hardTags(context).add(Tags.TAG_PRIMITIVE_FIRST);
  }

  public TSSet<String> hardTags(Context context) {
    return baseTags(context).add(Tags.TAG_PRIMITIVE_HARD);
  }

  protected ROList<Action> getActions() {
    return ROList.empty;
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

  public PrimitiveCursor createSelection(
      final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
    return new PrimitiveCursor(this, context, leadFirst, beginOffset, endOffset);
  }

  protected void commit() {}

  public void idleResplit(final Context context) {
    if (idleResplit == null && canExpand) {
      idleResplit = new IterationResplit(context);
      context.addIteration(idleResplit);
    }
  }

  @Override
  public VisualParent parent() {
    return parent;
  }

  public void tagsChanged(final Context context) {
    brickStyle.update(context);
    for (final Line line : lines) {
      line.styleChanged(context, brickStyle);
    }
    if (selection != null) context.selectionTagsChanged();
  }

  @Override
  public Brick createOrGetFirstBrick(final Context context) {
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

  @Override
  public void compact(final Context context) {
    final ResplitResult result = new ResplitResult();
    boolean rebreak = false;
    for (int i = lines.size() - 1; i >= 0; --i) {
      final Line line = lines.get(i);
      if (line.brick == null) continue;
      final int edge = line.brick.converseEdge();
      if (!rebreak && edge > context.edge) {
        rebreak = true;
      }
      if (line.hard && rebreak) {
        result.merge(resplitOne(context, i));
        rebreak = false;
      }
    }
    final boolean oldCanExpand = canExpand;
    canExpand = hardLineCount < lines.size();
    if (canExpand && !oldCanExpand) {
      context.foreground.splitPrimitives.add(this);
    }
  }

  @Override
  public void expand(final Context context) {}

  @Override
  public void getLeafPropertiesForTagsChange(
      final Context context,
      TSList<ROPair<Brick, Brick.Properties>> brickProperties,
      final TagsChange change) {
    for (Line line : lines) {
      if (line.brick == null) continue;
      brickProperties.add(
          new ROPair<>(line.brick, line.brick.getPropertiesForTagsChange(context, change)));
    }
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (idleResplit != null) idleResplit.destroy();
    if (selection != null) context.clearSelection();
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
    if (canExpand) context.foreground.splitPrimitives.remove(this);
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
    canExpand = false;
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    value.selectInto(context);
    return true;
  }

  @Override
  public Hoverable hover(final Context context, final Vector point) {
    if (parent != null) {
      return parent.hover(context, point);
    }
    return null;
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
      final int converse;
      if (line.brick == null) {
        final Style style =
            j == 0 ? brickStyle.firstStyle : j == i ? brickStyle.hardStyle : brickStyle.softStyle;
        font = Context.getFont(style, context);
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
        final Style style = brickStyle.softStyle;
        final Font font = Context.getFont(style, context);
        final Alignment alignment = atom.findAlignment(style.alignment);
        final int converse;
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

  private TSSet<String> baseTags(Context context) {
    return atomVisual().getTags(context).addAll(spec.tags).add(Tags.TAG_PART_PRIMITIVE);
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

    public ResplitResult build(final Line line, final Font font, final int converse) {
      final ResplitResult result = new ResplitResult();
      final int width = font.getWidth(text);
      final int edge = converse + width;
      int split;
      if (converse < context.edge && edge > context.edge) {
        final I18nEngine.Walker lineIter = context.i18n.lineWalker(text);
        final int edgeOffset = context.edge - converse;
        final int under = font.getIndexAtConverse(text, edgeOffset);
        if (under == text.length()) split = under;
        else {
          split = lineIter.preceding(under + 1);
          if (split == 0 || split == I18nEngine.DONE) {
            final I18nEngine.Walker clusterIter = context.i18n.glyphWalker(text);
            split = clusterIter.preceding(under + 1);
          }
          if (split < 4 || split == I18nEngine.DONE) {
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

    private final ValuePrimitive value;
    private final int beginOffset;
    private final int endOffset;
    private final boolean leadFirst;

    public PrimitiveSelectionState(
        final ValuePrimitive value,
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

  public static class PrimitiveCursor extends Cursor {
    public final RangeAttachment range;
    public final VisualFrontPrimitive visualPrimitive;
    private final ValuePrimitive.Listener clusterListener;
    private final TSList<Action> actions;
    I18nEngine.Walker clusterIterator;

    public PrimitiveCursor(
        final VisualFrontPrimitive visualPrimitive,
        final Context context,
        final boolean leadFirst,
        final int beginOffset,
        final int endOffset) {
      this.visualPrimitive = visualPrimitive;
      range = new RangeAttachment(visualPrimitive, true);
      range.setStyle(context, getBorderStyle(context).obbox);
      range.leadFirst = leadFirst;
      range.setOffsets(context, beginOffset, endOffset);
      clusterIterator = context.i18n.glyphWalker(visualPrimitive.value.get());
      clusterListener = new ClusterIteratorUpdater(this, visualPrimitive);
      visualPrimitive.value.addListener(this.clusterListener);
      this.actions =
          TSList.of(
              new VisualFrontPrimitive.ActionExit(this),
              new VisualFrontPrimitive.ActionNext(this),
              new VisualFrontPrimitive.ActionPrevious(this),
              new VisualFrontPrimitive.ActionNextElement(this),
              new VisualFrontPrimitive.ActionPreviousElement(this),
              new VisualFrontPrimitive.ActionNextWord(this),
              new VisualFrontPrimitive.ActionPreviousWord(this),
              new VisualFrontPrimitive.ActionLineBegin(this),
              new VisualFrontPrimitive.ActionLineEnd(this),
              new VisualFrontPrimitive.ActionNextLine(this),
              new VisualFrontPrimitive.ActionPreviousLine(this),
              new VisualFrontPrimitive.ActionCopy(this),
              new VisualFrontPrimitive.ActionGatherNext(this),
              new VisualFrontPrimitive.ActionGatherNextWord(this),
              new VisualFrontPrimitive.ActionGatherNextLineEnd(this),
              new VisualFrontPrimitive.ActionGatherNextLine(this),
              new VisualFrontPrimitive.ActionReleaseNext(this),
              new VisualFrontPrimitive.ActionReleaseNextWord(this),
              new VisualFrontPrimitive.ActionReleaseNextLineEnd(this),
              new VisualFrontPrimitive.ActionReleaseNextLine(this),
              new VisualFrontPrimitive.ActionGatherPrevious(this),
              new VisualFrontPrimitive.ActionGatherPreviousWord(this),
              new VisualFrontPrimitive.ActionGatherPreviousLineStart(this),
              new VisualFrontPrimitive.ActionGatherPreviousLine(this),
              new VisualFrontPrimitive.ActionReleasePrevious(this),
              new VisualFrontPrimitive.ActionReleasePreviousWord(this),
              new VisualFrontPrimitive.ActionReleasePreviousLineStart(this),
              new VisualFrontPrimitive.ActionReleasePreviousLine(this, beginOffset));
      actions.addAll(visualPrimitive.getActions());
      context.addActions(actions);
    }

    private int preceding(final int offset) {
      return preceding(clusterIterator, offset);
    }

    private int preceding(final I18nEngine.Walker iter, final int offset) {
      int to = iter.preceding(offset);
      if (to == I18nEngine.DONE) to = 0;
      return Math.max(0, to);
    }

    public int preceding() {
      return preceding(clusterIterator, range.beginOffset);
    }

    public int following(final int offset) {
      return following(clusterIterator, offset);
    }

    private int following(final I18nEngine.Walker iter, final int offset) {
      int to = iter.following(offset);
      if (to == I18nEngine.DONE) to = visualPrimitive.value.length();
      return Math.min(visualPrimitive.value.length(), to);
    }

    public int following() {
      return following(clusterIterator, range.endOffset);
    }

    private int nextWord(Context context, final int source) {
      final I18nEngine.Walker iter = context.i18n.wordWalker(visualPrimitive.value.get());
      return following(iter, source);
    }

    private int previousWord(Context context, final int source) {
      final I18nEngine.Walker iter = context.i18n.wordWalker(visualPrimitive.value.get());
      return preceding(iter, source);
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
    public void clear(final Context context) {
      context.removeActions(actions);
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
    public Path getSyntaxPath() {
      return visualPrimitive.value.getSyntaxPath().add(String.valueOf(range.beginOffset));
    }

    @Override
    public void tagsChanged(final Context context) {
      range.setStyle(context, getBorderStyle(context).obbox);
      super.tagsChanged(context);
    }

    @Override
    public ROSet<String> getTags(final Context context) {
      return visualPrimitive.baseTags(context).ro();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    private static class ClusterIteratorUpdater implements ValuePrimitive.Listener {
      private final VisualFrontPrimitive visualPrimitive;
      private final PrimitiveCursor primitiveCursor;

      public ClusterIteratorUpdater(
          PrimitiveCursor primitiveCursor, VisualFrontPrimitive visualPrimitive) {
        this.visualPrimitive = visualPrimitive;
        this.primitiveCursor = primitiveCursor;
      }

      @Override
      public void set(final Context context, final String text) {
        primitiveCursor.clusterIterator = context.i18n.glyphWalker(visualPrimitive.value.get());
      }

      @Override
      public void added(final Context context, final int index, final String text) {
        primitiveCursor.clusterIterator = context.i18n.glyphWalker(visualPrimitive.value.get());
      }

      @Override
      public void removed(final Context context, final int index, final int count) {
        primitiveCursor.clusterIterator = context.i18n.glyphWalker(visualPrimitive.value.get());
      }
    }
  }

  private static class DataListener implements ValuePrimitive.Listener {
    private final VisualFrontPrimitive visualFrontPrimitive;

    public DataListener(VisualFrontPrimitive visualFrontPrimitive) {
      this.visualFrontPrimitive = visualFrontPrimitive;
    }

    @Override
    public void set(final Context context, final String text) {
      visualFrontPrimitive.set(context, text);
      visualFrontPrimitive.idleLayBricks(context, 0, visualFrontPrimitive.lines.size());
    }

    @Override
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
        segment = segments.removeLast();
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

    @Override
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

  private static class ActionPrevious implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionPrevious(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "previous";
    }

    @Override
    public void run(final Context context) {
      primitiveCursor.visualPrimitive.parent.selectPrevious(context);
    }
  }

  private static class ActionNextElement implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionNextElement(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "next_element";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.following();
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionPreviousElement implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionPreviousElement(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "previous_element";
    }

    @Override
    public void run(final Context context) {
      final int newIndex = primitiveCursor.preceding();
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionNextWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionNextWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "next_word";
    }

    @Override
    public void run(final Context context) {
      final int newIndex = primitiveCursor.nextWord(context, primitiveCursor.range.endOffset);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionPreviousWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionPreviousWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "previous_word";
    }

    @Override
    public void run(final Context context) {
      final int newIndex = primitiveCursor.previousWord(context, primitiveCursor.range.beginOffset);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionLineBegin implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionLineBegin(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "line_begin";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.startOfLine(primitiveCursor.range.beginLine);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionLineEnd implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionLineEnd(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "line_end";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.endOfLine(primitiveCursor.range.endLine);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionNextLine implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionNextLine(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "next_line";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          primitiveCursor.nextLine(primitiveCursor.range.endLine, primitiveCursor.range.endOffset);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionPreviousLine implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionPreviousLine(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "previous_line";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          primitiveCursor.previousLine(
              primitiveCursor.range.beginLine, primitiveCursor.range.beginOffset);
      if (primitiveCursor.range.beginOffset == newIndex
          && primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setOffsets(context, newIndex);
    }
  }

  private static class ActionCopy implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionCopy(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "copy";
    }

    @Override
    public void run(final Context context) {

      context.copy(
          primitiveCursor
              .visualPrimitive
              .value
              .get()
              .substring(primitiveCursor.range.beginOffset, primitiveCursor.range.endOffset));
    }
  }

  private static class ActionGatherNext implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherNext(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_next";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.following();
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionGatherNextWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherNextWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_next_word";
    }

    @Override
    public void run(final Context context) {
      final int newIndex = primitiveCursor.nextWord(context, primitiveCursor.range.endOffset);
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionGatherNextLineEnd implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherNextLineEnd(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_next_line_end";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.endOfLine(primitiveCursor.range.endLine);
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionGatherNextLine implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherNextLine(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_next_line";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          primitiveCursor.nextLine(primitiveCursor.range.endLine, primitiveCursor.range.endOffset);
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionReleaseNext implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleaseNext(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_next";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.max(
              primitiveCursor.range.beginOffset,
              primitiveCursor.preceding(primitiveCursor.range.endOffset));
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionReleaseNextWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleaseNextWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_next_word";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.max(
              primitiveCursor.range.beginOffset,
              primitiveCursor.previousWord(context, primitiveCursor.range.endOffset));
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionReleaseNextLineEnd implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleaseNextLineEnd(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_next_line_end";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.max(
              primitiveCursor.range.beginOffset,
              primitiveCursor.startOfLine(primitiveCursor.range.endLine));
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionReleaseNextLine implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleaseNextLine(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_next_line";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.max(
              primitiveCursor.range.beginOffset,
              primitiveCursor.previousLine(
                  primitiveCursor.range.endLine, primitiveCursor.range.endOffset));
      if (primitiveCursor.range.endOffset == newIndex) return;
      primitiveCursor.range.setEndOffset(context, newIndex);
    }
  }

  private static class ActionGatherPrevious implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherPrevious(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_previous";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.preceding();
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionGatherPreviousWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherPreviousWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_previous_word";
    }

    @Override
    public void run(final Context context) {

      final int newIndex = primitiveCursor.previousWord(context, primitiveCursor.range.beginOffset);
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionGatherPreviousLineStart implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherPreviousLineStart(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_previous_line_start";
    }

    @Override
    public void run(final Context context) {
      final int newIndex = primitiveCursor.startOfLine(primitiveCursor.range.beginLine);
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionGatherPreviousLine implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionGatherPreviousLine(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "gather_previous_line";
    }

    @Override
    public void run(final Context context) {
      final int newIndex =
          primitiveCursor.previousLine(
              primitiveCursor.range.beginLine, primitiveCursor.range.beginOffset);
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionReleasePrevious implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleasePrevious(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_previous";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.min(
              primitiveCursor.range.endOffset,
              primitiveCursor.following(primitiveCursor.range.beginOffset));
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionReleasePreviousWord implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleasePreviousWord(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_previous_word";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.min(
              primitiveCursor.range.endOffset,
              primitiveCursor.nextWord(context, primitiveCursor.range.beginOffset));
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionReleasePreviousLineStart implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionReleasePreviousLineStart(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "release_previous_line_start";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.min(
              primitiveCursor.range.endOffset,
              primitiveCursor.endOfLine(primitiveCursor.range.beginLine));
      if (primitiveCursor.range.beginOffset == newIndex) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionReleasePreviousLine implements Action {
    private final PrimitiveCursor primitiveCursor;
    private final int beginOffset;

    public ActionReleasePreviousLine(PrimitiveCursor primitiveCursor, final int beginOffset) {
      this.primitiveCursor = primitiveCursor;
      this.beginOffset = beginOffset;
    }

    public String id() {
      return "release_previous_line";
    }

    @Override
    public void run(final Context context) {

      final int newIndex =
          Math.min(
              primitiveCursor.range.endOffset,
              primitiveCursor.nextLine(
                  primitiveCursor.range.beginLine, primitiveCursor.range.beginOffset));
      if (newIndex == beginOffset) return;
      primitiveCursor.range.setBeginOffset(context, newIndex);
    }
  }

  private static class ActionNext implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionNext(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "next";
    }

    @Override
    public void run(final Context context) {
      primitiveCursor.visualPrimitive.parent.selectNext(context);
    }
  }

  private static class ActionExit implements Action {
    private final PrimitiveCursor primitiveCursor;

    public ActionExit(PrimitiveCursor primitiveCursor) {
      this.primitiveCursor = primitiveCursor;
    }

    public String id() {
      return "exit";
    }

    @Override
    public void run(final Context context) {

      if (primitiveCursor.visualPrimitive.value.atomParentRef == null) return;
      primitiveCursor.visualPrimitive.value.atomParentRef.selectAtomParent(context);
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
      context.foreground.setCornerstone(
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
  }

  public static class PrimitiveHoverable extends Hoverable {
    private final VisualFrontPrimitive visualFrontPrimitive;
    RangeAttachment range;

    PrimitiveHoverable(VisualFrontPrimitive visualFrontPrimitive, final Context context) {
      range = new RangeAttachment(visualFrontPrimitive, false);
      range.setStyle(
          context, getBorderStyle(context, visualFrontPrimitive.baseTags(context)).obbox);
      this.visualFrontPrimitive = visualFrontPrimitive;
    }

    public void setPosition(final Context context, final int offset) {
      range.setOffsets(context, offset);
    }

    @Override
    public void clear(final Context context) {
      range.destroy(context);
      visualFrontPrimitive.hoverable = null;
    }

    @Override
    public void click(final Context context) {
      visualFrontPrimitive.select(context, true, range.beginOffset, range.endOffset);
    }

    @Override
    public VisualAtom atom() {
      return visualFrontPrimitive.parent.atomVisual();
    }

    @Override
    public Visual visual() {
      return visualFrontPrimitive;
    }

    @Override
    public void tagsChanged(final Context context) {
      range.setStyle(
          context, getBorderStyle(context, visualFrontPrimitive.baseTags(context)).obbox);
    }
  }

  public static class Line implements BrickInterface {
    public final boolean hard;
    private final VisualFrontPrimitive visualFrontPrimitive;
    public int offset;
    public String text;
    public BrickLine brick;
    public int index;

    private Line(VisualFrontPrimitive visualFrontPrimitive, final boolean hard) {
      this.hard = hard;
      this.visualFrontPrimitive = visualFrontPrimitive;
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

    public Hoverable hover(final Context context, final Vector point) {
      if (visualFrontPrimitive.selection == null) {
        final Hoverable out = visualFrontPrimitive.hover(context, point);
        if (out != null) return out;
      }
      if (visualFrontPrimitive.hoverable == null) {
        visualFrontPrimitive.hoverable =
            new VisualFrontPrimitive.PrimitiveHoverable(visualFrontPrimitive, context);
      }
      visualFrontPrimitive.hoverable.setPosition(context, offset + brick.getUnder(context, point));
      return visualFrontPrimitive.hoverable;
    }

    @Override
    public VisualLeaf getVisual() {
      return visualFrontPrimitive;
    }

    @Override
    public Brick createPrevious(final Context context) {
      return createPreviousBrick(context);
    }

    public Brick createPreviousBrick(final Context context) {
      if (index == 0) return visualFrontPrimitive.parent.createPreviousBrick(context);
      return visualFrontPrimitive.lines.get(index - 1).createBrick(context);
    }

    public Brick createBrick(final Context context) {
      if (brick != null) return null;
      createBrickInternal(context);
      if (visualFrontPrimitive.selection != null
          && (visualFrontPrimitive.selection.range.beginLine == Line.this
              || visualFrontPrimitive.selection.range.endLine == Line.this))
        visualFrontPrimitive.selection.range.nudge(context);
      return brick;
    }

    public Brick createBrickInternal(final Context context) {
      brick = new BrickLine(context, this);
      styleChanged(context, visualFrontPrimitive.brickStyle);
      brick.setText(context, text);
      return brick;
    }

    public void styleChanged(final Context context, final BrickStyle style) {
      if (brick == null) return;
      brick.setStyle(
          context, index == 0 ? style.firstStyle : hard ? style.hardStyle : style.softStyle);
    }

    @Override
    public Brick createNext(final Context context) {
      return createNextBrick(context);
    }

    public Brick createNextBrick(final Context context) {
      if (index == visualFrontPrimitive.lines.size() - 1) {
        return visualFrontPrimitive.parent.createNextBrick(context);
      }
      return visualFrontPrimitive.lines.get(index + 1).createBrick(context);
    }

    @Override
    public void brickDestroyed(final Context context) {
      brick = null;
    }

    @Override
    public Alignment findAlignment(final Style style) {
      return visualFrontPrimitive.parent.atomVisual().findAlignment(style.alignment);
    }

    @Override
    public TSSet<String> getTags(final Context context) {
      return (index == 0
          ? visualFrontPrimitive.firstTags(context)
          : hard ? visualFrontPrimitive.hardTags(context) : visualFrontPrimitive.softTags(context));
    }

    public Brick createOrGetBrick(final Context context) {
      if (brick != null) return brick;
      return createBrickInternal(context);
    }

    public void idleResplit(final Context context) {
      visualFrontPrimitive.idleResplit(context);
    }
  }

  public class BrickStyle {
    public Style softStyle;
    public Style hardStyle;
    public Style firstStyle;

    BrickStyle(final Context context) {
      update(context);
    }

    public void update(final Context context) {
      firstStyle = context.getStyle(firstTags(context).ro());
      hardStyle = context.getStyle(hardTags(context).ro());
      softStyle = context.getStyle(softTags(context).ro());
    }
  }

  private class IterationResplit extends IterationTask {
    final Context context;

    private IterationResplit(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      Line line = lines.get(0);
      if (line.brick == null) line = lines.last();
      return 181.0 - (500.0 / (line.brick.parent.index + 200.0 / line.brick.index));
    }

    @Override
    protected boolean runImplementation(final IterationContext iterationContext) {
      final ResplitResult result = new ResplitResult();
      boolean go = true;
      for (int i = lines.size() - 1; i >= 0; --i) {
        final Line line = lines.get(i);
        if (line.brick == null) continue;
        final int converseEdge = line.brick.converseEdge();
        if (converseEdge <= context.edge
            && converseEdge * context.retryExpandFactor >= context.edge) go = false;
        if (line.hard) {
          if (go) result.merge(resplitOne(context, i));
          else go = true;
        }
      }
      final boolean oldCanExpand = canExpand;
      canExpand = hardLineCount < lines.size();
      if (canExpand && !oldCanExpand) {
        context.foreground.splitPrimitives.add(VisualFrontPrimitive.this);
      } else if (!canExpand && oldCanExpand) {
        context.foreground.splitPrimitives.remove(VisualFrontPrimitive.this);
      }
      return false;
    }

    @Override
    protected void destroyed() {
      idleResplit = null;
    }
  }
}