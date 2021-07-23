package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.FreeDisplayNode;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.List;

/**
 * Each row contains multiple pieces, with corresponding pieces in multiple rows to be laid out in
 * columns. Table is additionally wrapped into columns, where the total table transverse exceeds a
 * maximum.
 */
public class ColumnarTableLayout implements FreeDisplayNode {
  public final Group group;
  private final double maxTransverse;
  int innerColumns;
  TSList<ROList<CourseDisplayNode>> rows = new TSList<>();
  private double usedTransverse;
  private double rowStride;
  private double rowPadConverseStart;
  private double rowPadConverseEnd;
  private double rowPadTransverseStart;
  private double rowPadTransverseEnd;
  private double outerColumnGap;

  public ColumnarTableLayout(final Display display, final double maxTransverse) {
    this.group = display.group();
    this.maxTransverse = maxTransverse;
  }

  public ColumnarTableLayout(final Context context, final double maxTransverse) {
    this(context.display, maxTransverse);
  }

  public void setOuterColumnGap(Context context, double span) {
    this.outerColumnGap = span * context.toPixels;
  }

  public void setRowStride(Context context, double span) {
    this.rowStride = span * context.toPixels;
  }

  public void setRowPadding(Context context, Padding padding) {
    this.rowPadConverseStart = padding.converseStart * context.toPixels;
    this.rowPadConverseEnd = padding.converseEnd * context.toPixels;
    this.rowPadTransverseStart = padding.converseStart * context.toPixels;
    this.rowPadTransverseEnd = padding.converseEnd * context.toPixels;
  }

  public void add(final ROList<CourseDisplayNode> row) {
    innerColumns = Math.max(innerColumns, row.size());
    rows.add(row);
    for (final DisplayNode node : row) group.add(node);
  }

  public void layout() {
    int outerColumnRowStartIndex = 0;
    double previousOuterColumnConverseEdge = 0;
    usedTransverse = 0;
    while (outerColumnRowStartIndex < rows.size()) {
      double newOuterColumnEdge = 0;
      int outerColumnRowEndIndex = outerColumnRowStartIndex;

      // Find the converse size of each column, and the number of rows that fit in the transverse
      // span
      final double[] innerColumnSpans = new double[innerColumns];
      final List<Double> baselines = new ArrayList<>();
      {
        double transverse = 0;
        for (int y = outerColumnRowStartIndex; y < rows.size(); ++y) {
          if (rowStride == 0) transverse += rowPadTransverseStart;
          double ascent = 0;
          double descent = 0;
          final ROList<CourseDisplayNode> row = rows.get(y);
          for (int x = 0; x < innerColumns; ++x) {
            final CourseDisplayNode cell = row.get(x);
            ascent = Math.max(ascent, cell.ascent());
            descent = Math.max(descent, cell.descent());
          }
          if (outerColumnRowEndIndex > outerColumnRowStartIndex
              && transverse + ascent + descent >= maxTransverse) {
            break;
          }
          for (int x = 0; x < innerColumns; ++x) {
            final DisplayNode cell = row.get(x);
            innerColumnSpans[x] = Math.max(innerColumnSpans[x], cell.converseSpan());
          }
          baselines.add(transverse + ascent);
          if (rowStride != 0) transverse += rowStride;
          else transverse += ascent + descent + rowPadTransverseEnd;
          outerColumnRowEndIndex += 1;
        }
        usedTransverse = Math.max(usedTransverse, transverse);
      }

      // Place everything
      for (int rowIndex = outerColumnRowStartIndex; rowIndex < outerColumnRowEndIndex; ++rowIndex) {
        final double baseline = baselines.get(rowIndex - outerColumnRowStartIndex);
        final ROList<CourseDisplayNode> row = rows.get(rowIndex);
        double converse = previousOuterColumnConverseEdge + rowPadConverseStart;
        for (int x = 0; x < innerColumns; ++x) {
          double useConverse;
          if (x == 0) {
            useConverse = converse + innerColumnSpans[x] - row.get(x).converseSpan();
          } else {
            useConverse = converse;
          }
          row.get(x).setBaselinePosition(new Vector(useConverse, baseline), false);
          converse += innerColumnSpans[x];
        }
        converse += rowPadConverseEnd;
        newOuterColumnEdge = Math.max(newOuterColumnEdge, converse);
      }

      //
      outerColumnRowStartIndex = outerColumnRowEndIndex;
      previousOuterColumnConverseEdge = newOuterColumnEdge + outerColumnGap;
    }
  }

  @Override
  public double converse() {
    return group.converse();
  }

  @Override
  public double transverse() {
    return group.transverse();
  }

  @Override
  public double transverseSpan() {
    return usedTransverse;
  }

  @Override
  public double converseSpan() {
    throw new Assertion();
  }

  @Override
  public void setConverse(double converse, boolean animate) {
    group.setConverse(converse, animate);
  }

  @Override
  public Object inner_() {
    return group.inner_();
  }

  @Override
  public void setPosition(Vector vector, boolean animate) {
    group.setPosition(vector, animate);
  }
}
