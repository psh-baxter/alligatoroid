package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.Hoverable;
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

public class VisualFieldPrimitive extends Visual implements VisualLeaf {

  public final FieldPrimitive value;
  // INVARIANT: Leaf nodes must always create at least one brick
  // INVARIANT: Always at least one line
  // TODO index line offsets for faster insert/remove
  private final FieldPrimitive.Listener dataListener;
  private final FrontPrimitiveSpec spec;
  public VisualParent parent;
  public int brickCount = 0;
  public PrimitiveHoverable hoverable;
  public CursorFieldPrimitive cursor;
  public TSList<Line> lines = new TSList<>();
  public int hardLineCount = 0;
  public boolean valid = true;

  public VisualFieldPrimitive(
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

  public void copy(Context context, int beginOffset, int endOffset) {
    context.copy(value.get().substring(beginOffset, endOffset));
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
    if (cursor != null) {
      cursor.range.setOffsets(
          context, Math.max(0, Math.min(text.length(), cursor.range.beginOffset)));
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
    if (cursor != null) {
      cursor.range.leadFirst = leadFirst;
      cursor.range.setOffsets(context, beginOffset, endOffset);
    } else {
      cursor = createSelection(context, leadFirst, beginOffset, endOffset);
      context.setCursor(cursor);
    }
  }

  public CursorFieldPrimitive createSelection(
      final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
    return context.cursorFactory.createFieldPrimitiveCursor(
        context, this, leadFirst, beginOffset, endOffset);
  }

  protected void commit() {}

  @Override
  public VisualParent parent() {
    return parent;
  }

  @Override
  public CreateBrickResult createOrGetCornerstoneCandidate(final Context context) {
    return CreateBrickResult.brick(lines.get(0).createOrGetBrick(context));
  }

  @Override
  public ExtendBrickResult createFirstBrick(final Context context) {
    return lines.get(0).createBrick(context);
  }

  @Override
  public ExtendBrickResult createLastBrick(final Context context) {
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
        resplitOne(context, i);
        anyOver = false;
        allUnder = true;
      }
    }
  }

  @Override
  public void compact(final Context context) {
    lines.get(0).brick.layoutPropertiesChanged(context);
  }

  @Override
  public void expand(final Context context) {
    lines.get(0).brick.layoutPropertiesChanged(context);
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
    if (cursor != null) context.clearCursor();
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
  public boolean selectIntoAnyChild(final Context context) {
    value.selectInto(context);
    return true;
  }

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    if (parent == null) return null;
    return parent.hover(context, point);
  }

  @Override
  public void notifyLastBrickCreated(Context context, Brick brick) {
    parent.notifyLastBrickCreated(context, brick);
  }

  @Override
  public void notifyFirstBrickCreated(Context context, Brick brick) {
    parent.notifyFirstBrickCreated(context, brick);
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
    if (cursor != null) {
      cursor.range.nudge(context);
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

  public static class PrimitiveCursorState implements CursorState {
    private final FieldPrimitive value;
    private final int beginOffset;
    private final int endOffset;
    private final boolean leadFirst;

    public PrimitiveCursorState(
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
      ((VisualFieldPrimitive) value.visual).select(context, leadFirst, beginOffset, endOffset);
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

  private static class DataListener implements FieldPrimitive.Listener {
    private final VisualFieldPrimitive visualFieldPrimitive;

    public DataListener(VisualFieldPrimitive visualFieldPrimitive) {
      this.visualFieldPrimitive = visualFieldPrimitive;
    }

    @Override
    public void changed(Context context, int index, int remove, String add) {
      removed(context, index, remove);
      added(context, index, add);
      if (this.visualFieldPrimitive.value.back.matcher != null) {
        boolean newValid =
            this.visualFieldPrimitive.value.back.matcher.match(
                context.env, this.visualFieldPrimitive.value.data.toString());
        if (newValid != visualFieldPrimitive.valid) {
          visualFieldPrimitive.valid = newValid;
          for (Line line : this.visualFieldPrimitive.lines) {
            if (line.brick == null) continue;
            line.brick.updateValid(context);
          }
        }
      }
    }

    public void added(final Context context, final int offset, final String text) {
      final TSList<String> segments = TSList.of(text.split("\n", -1));
      if (segments.isEmpty()) return;
      segments.reverse();
      final int originalIndex = visualFieldPrimitive.findContaining(offset);
      int index = originalIndex;
      Line line = visualFieldPrimitive.lines.get(index);

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
        line = new Line(visualFieldPrimitive, true);
        visualFieldPrimitive.hardLineCount += 1;
        line.setText(context, segment);
        line.setIndex(context, index);
        movingOffset += 1;
        line.offset = movingOffset;
        visualFieldPrimitive.lines.insert(index, line);
      }
      final int lastLineCreated = index + 1;
      if (remainder != null) line.setText(context, line.text + remainder);

      // Renumber/adjust offset of following lines
      visualFieldPrimitive.renumber(index, movingOffset);

      if (visualFieldPrimitive.cursor != null) {
        final int newBegin;
        if (visualFieldPrimitive.cursor.range.beginOffset < offset)
          newBegin = visualFieldPrimitive.cursor.range.beginOffset;
        else newBegin = visualFieldPrimitive.cursor.range.beginOffset + text.length();
        visualFieldPrimitive.cursor.range.setOffsets(context, newBegin);
      }

      visualFieldPrimitive.idleLayBricks(
          context, firstLineCreated, lastLineCreated - firstLineCreated);
    }

    public void removed(final Context context, final int offset, final int count) {
      int remaining = count;
      final Line base = visualFieldPrimitive.lines.get(visualFieldPrimitive.findContaining(offset));

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
        final Line line = visualFieldPrimitive.lines.get(index++);
        if (line.hard) {
          remaining -= 1;
        }
        final int exciseEnd = Math.min(remaining, line.text.length());
        base.setText(context, base.text + line.text.substring(exciseEnd));
        remaining -= exciseEnd;
        if (line.hard) visualFieldPrimitive.hardLineCount -= 1;
        removeLines += 1;
      }
      final TSList<Line> sublist =
          visualFieldPrimitive.lines.sublist(base.index + 1, base.index + 1 + removeLines);
      final ROList<Line> oldSublist = sublist.mut();
      sublist.clear();
      for (final Line line : oldSublist) line.destroy(context);
      for (int i = base.index + 1; i < visualFieldPrimitive.lines.size(); ++i) {
        Line line = visualFieldPrimitive.lines.get(i);
        line.index = base.index + 1 + i;
        line.offset -= count;
      }
      if (visualFieldPrimitive.hoverable != null) {
        if (visualFieldPrimitive.hoverable.range.beginOffset >= offset + count) {
          visualFieldPrimitive.hoverable.range.setOffsets(
              context, visualFieldPrimitive.hoverable.range.beginOffset - (offset + count));
        } else if (visualFieldPrimitive.hoverable.range.beginOffset >= offset
            || visualFieldPrimitive.hoverable.range.endOffset >= offset) {
          context.clearHover();
        }
      }
      if (visualFieldPrimitive.cursor != null) {
        int newBegin = visualFieldPrimitive.cursor.range.beginOffset;
        int newEnd = visualFieldPrimitive.cursor.range.endOffset;
        if (newBegin >= offset + count) newBegin = newBegin - count;
        else if (newBegin >= offset) newBegin = offset;
        if (newEnd >= offset + count) newEnd = newEnd - count;
        else if (newEnd >= offset) newEnd = offset;
        visualFieldPrimitive.cursor.range.setOffsets(context, newBegin, newEnd);
      }
    }
  }

  public static class RangeAttachment {
    private final boolean forSelection;
    private final VisualFieldPrimitive visualFieldPrimitive;
    public CursorAttachment cursor;
    public int beginOffset;
    public int endOffset;
    public Line beginLine;
    public Line endLine;
    public boolean leadFirst;
    TextBorderAttachment border;
    TSSet<BoundsListener> listeners = new TSSet<>();
    private ObboxStyle style;

    public RangeAttachment(VisualFieldPrimitive visualFieldPrimitive, final boolean forSelection) {
      this.forSelection = forSelection;
      this.visualFieldPrimitive = visualFieldPrimitive;
    }

    public void setOffsets(final Context context, final int beginOffset, final int endOffset) {
      setOffsetsInternal(context, beginOffset, endOffset);
    }

    private void setOffsetsInternal(
        final Context context, final int beginOffset, final int endOffset) {
      final boolean wasPoint = this.beginOffset == this.endOffset;
      this.beginOffset = Math.max(0, Math.min(visualFieldPrimitive.value.length(), beginOffset));
      this.endOffset =
          Math.max(beginOffset, Math.min(visualFieldPrimitive.value.length(), endOffset));
      if (beginOffset == endOffset) {
        if (border != null) {
          border.destroy(context);
          border = null;
        }
        if (cursor == null) {
          cursor = new CursorAttachment(context);
          cursor.setStyle(context, style);
        }
        final int index = visualFieldPrimitive.findContaining(beginOffset);
        beginLine = endLine = visualFieldPrimitive.lines.get(index);
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
        final int beginIndex = visualFieldPrimitive.findContaining(beginOffset);
        if (beginLine != null && beginLine.index == beginIndex) {
          newFirstBrick = beginLine.brick;
        } else {
          beginLine = visualFieldPrimitive.lines.get(beginIndex);
          if (beginLine.brick != null) newFirstBrick = beginLine.brick;
          else newFirstBrick = null;
        }
        int newFirstIndex = beginOffset - beginLine.offset;
        final int endIndex = visualFieldPrimitive.findContaining(endOffset);
        if (endLine != null && endLine.index == endIndex) {
          newLastBrick = endLine.brick;
        } else {
          endLine = visualFieldPrimitive.lines.get(endIndex);
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
          visualFieldPrimitive.lines.get(index).createOrGetBrick(context),
          () -> {
            for (int at = index - 1; at >= 0; --at) {
              final Brick found = visualFieldPrimitive.lines.get(at).brick;
              if (found != null) return found;
            }
            return visualFieldPrimitive.parent.findPreviousBrick(context);
          },
          () -> {
            for (int at = index + 1; at < visualFieldPrimitive.lines.size(); ++at) {
              final Brick found = visualFieldPrimitive.lines.get(at).brick;
              if (found != null) return found;
            }
            return visualFieldPrimitive.parent.findNextBrick(context);
          });
    }

    public void setOffsets(final Context context, final int offset) {
      setOffsetsInternal(context, offset, offset);
    }

    public void setBeginOffset(final Context context, final int offset) {
      if (offset >= endOffset) {
        leadFirst = false;
        setOffsetsInternal(context, endOffset, offset);
      } else {
        leadFirst = true;
        setOffsetsInternal(context, offset, endOffset);
      }
    }

    public void setEndOffset(final Context context, final int offset) {
      if (offset <= beginOffset) {
        leadFirst = true;
        setOffsetsInternal(context, offset, beginOffset);
      } else {
        leadFirst = false;
        setOffsetsInternal(context, beginOffset, offset);
      }
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
    private final VisualFieldPrimitive visual;
    RangeAttachment range;

    PrimitiveHoverable(VisualFieldPrimitive visual, final Context context) {
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
    public Visual visual() {
      return visual;
    }
  }

  public static class Line implements BrickInterface {
    public final boolean hard;
    public final VisualFieldPrimitive visual;
    public int offset;
    public String text;
    public BrickLine brick;
    public int index;

    private Line(VisualFieldPrimitive visual, final boolean hard) {
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
      if (this.index == 0 && brick != null) brick.layoutPropertiesChanged(context);
      this.index = index;
    }

    public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
      if (visual.cursor == null) {
        final ROPair<Hoverable, Boolean> out = visual.hover(context, point);
        if (out != null) return out;
      }
      boolean changed = false;
      if (visual.hoverable == null) {
        visual.hoverable = new VisualFieldPrimitive.PrimitiveHoverable(visual, context);
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
    public ExtendBrickResult createPrevious(final Context context) {
      return createPreviousBrick(context);
    }

    public ExtendBrickResult createPreviousBrick(final Context context) {
      if (index == 0) return visual.parent.createPreviousBrick(context);
      return visual.lines.get(index - 1).createBrick(context);
    }

    public ExtendBrickResult createBrick(final Context context) {
      if (brick != null) return ExtendBrickResult.exists();
      createBrickInternal(context);
      if (visual.cursor != null
          && (visual.cursor.range.beginLine == Line.this
              || visual.cursor.range.endLine == Line.this)) visual.cursor.range.nudge(context);
      return ExtendBrickResult.brick(brick);
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
      if (index == 0) visual.notifyFirstBrickCreated(context, brick);
      if (index + 1 == visual.lines.size()) visual.notifyLastBrickCreated(context, brick);
      return brick;
    }

    @Override
    public ExtendBrickResult createNext(final Context context) {
      return createNextBrick(context);
    }

    public ExtendBrickResult createNextBrick(final Context context) {
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
