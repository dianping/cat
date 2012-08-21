package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.meta.ColumnMeta;

public class DefaultStatement implements Statement {

	@Override
	public List<ColumnMeta> getSelectColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Index getIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RowFilter getRowFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowFilter(RowFilter rowFilter) {
		// TODO Auto-generated method stub

	}

}
