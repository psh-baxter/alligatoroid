package com.zarbosoft.merman.editor.display.derived;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.CourseDisplayNode;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.List;

public class ColumnarTableLayout {
	public final Group group;
	private final int maxTransverse;
	int columns;
	TSList<ROList<CourseDisplayNode>> rows = new TSList<>();

	public ColumnarTableLayout(final Display display, final int maxTransverse) {
		this.group = display.group();
		this.maxTransverse = maxTransverse;
	}

	public ColumnarTableLayout(final Context context, final int maxTransverse) {
		this(context.display, maxTransverse);
	}

	public void add(final ROList<CourseDisplayNode> row) {
		columns = Math.max(columns, row.size());
		rows.add(row);
		for (final DisplayNode node : row)
			group.add(node);
	}

	public void layout() {
		int start = 0;
		int columnEdge = 0;
		while (start < rows.size()) {
			int newColumnEdge = 0;
			int end = start;

			// Find the converse size of each column, and the number of rows that fit in the transverse span
			final int[] columnSpans = new int[columns];
			final List<Pair<Integer, Integer>> rowStarts = new ArrayList<>();
			{
				int transverse = 0;
				for (int y = start; y < rows.size(); ++y) {
					int ascent = 0;
					int descent = 0;
					final ROList<CourseDisplayNode> row = rows.get(y);
					for (int x = 0; x < columns; ++x) {
						final DisplayNode cell = row.get(x);
						if (cell instanceof Text) {
							ascent = Math.max(ascent, ((Text) cell).font().getAscent());
							descent = Math.max(descent, ((Text) cell).font().getDescent());
						}
						if (!(cell instanceof Text))
							ascent = Math.max(ascent, cell.transverseSpan());
					}
					if (end > start && transverse + ascent + descent >= maxTransverse) {
						break;
					}
					for (int x = 0; x < columns; ++x) {
						final DisplayNode cell = row.get(x);
						columnSpans[x] = Math.max(columnSpans[x], cell.converseSpan());
					}
					rowStarts.add(new Pair<>(transverse, ascent));
					transverse += ascent + descent;
					end += 1;
				}
			}

			// Place everything
			for (int y = start; y < end; ++y) {
				final ROList<CourseDisplayNode> row = rows.get(y);
				int converse = columnEdge;
				for (int x = 0; x < columns; ++x) {
					final CourseDisplayNode cell = row.get(x);
					final Pair<Integer, Integer> rowTransverse = rowStarts.get(y - start);
					int transverse = rowTransverse.first;
						transverse += rowTransverse.second;
					cell.setBaselinePosition(new Vector(converse, transverse), false);
					converse += columnSpans[x];
				}
				newColumnEdge = Math.max(newColumnEdge, converse);
			}

			//
			start = end;
			columnEdge = newColumnEdge;
		}
	}
}
