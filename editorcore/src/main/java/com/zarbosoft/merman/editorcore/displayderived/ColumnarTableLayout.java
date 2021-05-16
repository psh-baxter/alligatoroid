package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.List;

/**
 * Each row contains multiple pieces, with corresponding pieces in multiple rows to be laid out in
 * columns. Table is additionally wrapped into columns, where the total table transverse exceeds a
 * maximum.
 */
public class ColumnarTableLayout {
  public final Group group;
  private final int maxTransverse;
  int innerColumns;
  TSList<ROList<CourseDisplayNode>> rows = new TSList<>();

  public ColumnarTableLayout(final Display display, final int maxTransverse) {
    this.group = display.group();
    this.maxTransverse = maxTransverse;
  }

  public ColumnarTableLayout(final Context context, final int maxTransverse) {
    this(context.display, maxTransverse);
  }

  public void add(final ROList<CourseDisplayNode> row) {
    innerColumns = Math.max(innerColumns, row.size());
    rows.add(row);
    for (final DisplayNode node : row) group.add(node);
  }

  public void layout() {
    int outerColumnRowStart = 0;
    int previousOuterColumnEdge = 0;
    while (outerColumnRowStart < rows.size()) {
      int newOuterColumnEdge = 0;
      int outerColumnRowEnd = outerColumnRowStart;

      // Find the converse size of each column, and the number of rows that fit in the transverse
      // span
      final double[] innerColumnSpans = new double[innerColumns];
      final List<LayoutInfo> rowStarts = new ArrayList<>();
      {
        double transverse = 0;
        for (int y = outerColumnRowStart; y < rows.size(); ++y) {
          double ascent = 0;
          double descent = 0;
          final ROList<CourseDisplayNode> row = rows.get(y);
          for (int x = 0; x < innerColumns; ++x) {
            final CourseDisplayNode cell = row.get(x);
            ascent = Math.max(ascent, cell.ascent());
            descent = Math.max(descent, cell.descent());
          }
          if (outerColumnRowEnd > outerColumnRowStart
              && transverse + ascent + descent >= maxTransverse) {
            break;
          }
          for (int x = 0; x < innerColumns; ++x) {
            final DisplayNode cell = row.get(x);
            innerColumnSpans[x] = Math.max(innerColumnSpans[x], cell.converseSpan());
          }
          rowStarts.add(new LayoutInfo(transverse, ascent));
          transverse += ascent + descent;
          outerColumnRowEnd += 1;
        }
      }

      // Place everything
      for (int rowIndex = outerColumnRowStart; rowIndex < outerColumnRowEnd; ++rowIndex) {
        final LayoutInfo rowTransverse = rowStarts.get(rowIndex - outerColumnRowStart);
        final ROList<CourseDisplayNode> row = rows.get(rowIndex);
        int converse = previousOuterColumnEdge;
        for (int x = 0; x < innerColumns; ++x) {
          row.get(x)
              .setBaselinePosition(
                  new Vector(converse, rowTransverse.transverse + rowTransverse.ascent), false);
          converse += innerColumnSpans[x];
        }
        newOuterColumnEdge = Math.max(newOuterColumnEdge, converse);
      }

      //
      outerColumnRowStart = outerColumnRowEnd;
      previousOuterColumnEdge = newOuterColumnEdge;
    }
  }

  private static class LayoutInfo {
    private final double transverse;
    private final double ascent;

    private LayoutInfo(double transverse, double ascent) {
      this.transverse = transverse;
      this.ascent = ascent;
    }
  }
}
