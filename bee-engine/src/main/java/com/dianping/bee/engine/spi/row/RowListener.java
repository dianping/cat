package com.dianping.bee.engine.spi.row;

import com.dianping.bee.engine.spi.meta.RowSet;

public interface RowListener {
	public void onRow(RowContext ctx);

	public void setRowFilter(RowFilter m_rowFilter);

	public RowSet getRowSet();
}
