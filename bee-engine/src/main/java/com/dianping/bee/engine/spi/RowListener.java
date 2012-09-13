package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.RowSet;

public interface RowListener {
	public RowSet getRowSet();

	public void onBegin(RowContext ctx);

	public void onEnd(RowContext ctx);

	public void onRow(RowContext ctx);

	public void setRowFilter(RowFilter m_rowFilter);
}
