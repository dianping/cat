package com.dianping.bee.engine.spi.row;

import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.DefaultCell;
import com.dianping.bee.engine.spi.meta.internal.DefaultRow;
import com.dianping.bee.engine.spi.meta.internal.DefaultRowSet;

public class DefaultRowListener implements RowListener {
	private RowFilter m_filter;

	private DefaultRowSet m_rowset;

	public DefaultRowListener(ColumnMeta[] selectColumns) {
		m_rowset = new DefaultRowSet(selectColumns);
	}

	@Override
	public void onRow(RowContext ctx) {
		if (m_filter == null || m_filter.filter(ctx)) {
			int cols = m_rowset.getColumnSize();
			Cell[] cells = new Cell[cols];

			for (int i = 0; i < cols; i++) {
				cells[i] = new DefaultCell(ctx.getColumn(i), ctx.getValue(i));
			}

			m_rowset.addRow(new DefaultRow(cells));
		}
	}

	public void setRowFilter(RowFilter filter) {
		m_filter = filter;
	}

	public RowSet getRowSet() {
		return m_rowset;
	}
}
