package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.RowSet;

public interface RowListener {
	public void onRow(RowContext ctx);

	public void setRowFilter(RowFilter m_rowFilter);

	public RowSet getRowSet();
}
